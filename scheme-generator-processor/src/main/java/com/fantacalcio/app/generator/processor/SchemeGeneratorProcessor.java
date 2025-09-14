package com.fantacalcio.app.generator.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.squareup.javapoet.ParameterSpec.Builder;
import com.fantacalcio.app.generator.api.GenerateScheme;
import com.fantacalcio.app.generator.api.GenerateSchemes;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SupportedAnnotationTypes("com.fantacalcio.app.generator.api.GenerateSchemes")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class SchemeGeneratorProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		
		for (Element element : roundEnv.getElementsAnnotatedWith(GenerateSchemes.class)) {
			GenerateSchemes schemes = element.getAnnotation(GenerateSchemes.class);
			for (GenerateScheme scheme : schemes.value()) {
				try {
					generateSchemeClass(scheme);
				} catch (IOException e) {
					processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.ERROR,
							"Failed to generate scheme: " + e.getMessage());
				}
			}
		}
		return true;
	}

	private void generateSchemeClass(GenerateScheme scheme) throws IOException {

	    // 1. constructs Names for existing domain classes
	    String domainPackage = "domainModel";
	    ClassName schemeBaseClass = ClassName.get(domainPackage, "Scheme");
	    ClassName starterLineUpClass = ClassName.get(domainPackage, "StarterLineUp");
	    String playerClassString = "Player"; 
	    // we lie about package structure to get imports for nested Player subtypes
	    ClassName gkClass = ClassName.get(domainPackage + "." + playerClassString, "Goalkeeper");
	    ClassName defClass = ClassName.get(domainPackage + "." + playerClassString, "Defender");
	    ClassName midClass = ClassName.get(domainPackage + "." + playerClassString, "Midfielder");
	    ClassName fwdClass = ClassName.get(domainPackage + "." + playerClassString, "Forward");
	   
	    // 2. constructs Names for types to be generated
		String targetPackage = domainPackage + "scheme";
		String generatedClassString = scheme.className();
		ClassName generatedClassName = ClassName.get(targetPackage, generatedClassString);
		ClassName stepsClassName = generatedClassName.nestedClass("StarterLineUpBuilderSteps");
		ClassName builderClassName = generatedClassName
				.nestedClass("StarterLineUpBuilder" + generatedClassString.substring(6));
	    ClassName readyForGk = stepsClassName.nestedClass("ReadyForGoalkeeper");
	    ClassName readyForDef = stepsClassName.nestedClass("ReadyForDefenders");
	    ClassName readyForMid = stepsClassName.nestedClass("ReadyForMidfielders");
	    ClassName readyForFwd = stepsClassName.nestedClass("ReadyForForwards");
	    
	    // 3. generates parameter lists once for reuse
	    List<ParameterSpec> goalieParams = generateParameters(gkClass, "goalie", 1);
	    List<ParameterSpec> defenderParams = generateParameters(defClass, "defender", scheme.defenders());
	    List<ParameterSpec> midfielderParams = generateParameters(midClass, "midfielder", scheme.midfielders());
	    List<ParameterSpec> forwardParams = generateParameters(fwdClass, "forward", scheme.forwards());

	    // 4. builds the abstract interface methods
	    String withGoalkeeperString = "withGoalkeeper";
	    String withDefendersString = "withDefenders";
	    String withMidfieldersString = "withMidfielders";
	    String withForwardsString = "withForwards";
	    
		MethodSpec withGoalieAbstract = MethodSpec.methodBuilder(withGoalkeeperString)
	    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
	    		.addParameters(goalieParams)
	    		.returns(readyForDef)
	    		.build();
		MethodSpec withDefendersAbstract = MethodSpec.methodBuilder(withDefendersString)
	    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
	    		.addParameters(defenderParams)
	    		.returns(readyForMid)
	    		.build();
		MethodSpec withMidfieldersAbstract = MethodSpec.methodBuilder(withMidfieldersString)
	    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
	    		.addParameters(midfielderParams)
	    		.returns(readyForFwd)
	    		.build();
		MethodSpec withForwardsAbstract = MethodSpec.methodBuilder(withForwardsString)
	    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
	    		.addParameters(forwardParams)
	    		.returns(starterLineUpClass)
	    		.build();

	    // 5. builds Step Builder Interfaces
	    TypeSpec readyForGkInterface = TypeSpec.interfaceBuilder("ReadyForGoalkeeper")
	    		.addMethod(withGoalieAbstract)
	    		.build();
	    TypeSpec readyForDefInterface = TypeSpec.interfaceBuilder("ReadyForDefenders")
	    		.addMethod(withDefendersAbstract)
	    		.build();
	    TypeSpec readyForMidInterface = TypeSpec.interfaceBuilder("ReadyForMidfielders")
	    		.addMethod(withMidfieldersAbstract)
	    		.build();
	    TypeSpec readyForForwInterface = TypeSpec.interfaceBuilder("ReadyForForwards")
	    		.addMethod(withForwardsAbstract)
	    		.build();
	    
	    // 6. builds the Steps container class
	    TypeSpec stepsClass = TypeSpec.classBuilder(stepsClassName.simpleName())
	            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.ABSTRACT)
	            .addType(readyForGkInterface)
	            .addType(readyForDefInterface)
	            .addType(readyForMidInterface)
	            .addType(readyForForwInterface)
	            .build();
	    
	    // 7. builds the concrete building methods 
	    MethodSpec withGoalkeeperConcrete = MethodSpec.methodBuilder(withGoalkeeperString)
	            .addAnnotation(Override.class)
	            .addModifiers(Modifier.PUBLIC)
	            .addParameters(goalieParams)
	            .addCode(implementorBody(goalieParams, true).build())
	            .addStatement("return this")
	            .returns(readyForDef)
	            .build();
	    
	    MethodSpec withDefendersConcrete = MethodSpec.methodBuilder(withDefendersString)
	            .addAnnotation(Override.class)
	            .addModifiers(Modifier.PUBLIC)
	            .addParameters(defenderParams)
	            .addCode(implementorBody(defenderParams, true).build())
	            .addStatement("return this")
	            .returns(readyForMid)
	            .build();
	    
	    MethodSpec withMidfieldersConcrete = MethodSpec.methodBuilder(withMidfieldersString)
	            .addAnnotation(Override.class)
	            .addModifiers(Modifier.PUBLIC)
	            .addParameters(midfielderParams)
	            .addCode(implementorBody(midfielderParams, true).build())
	            .addStatement("return this")
	            .returns(readyForFwd)
	            .build();
	    
	    String defendersList = defenderParams.stream().map(p -> p.name).collect(Collectors.joining(", "));
	    String midfieldersList = midfielderParams.stream().map(p -> p.name).collect(Collectors.joining(", "));
	    String forwardsList = forwardParams.stream().map(p -> p.name).collect(Collectors.joining(", "));

		MethodSpec withForwardsConcrete = MethodSpec.methodBuilder(withForwardsString)
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameters(forwardParams)
				.addCode(implementorBody(forwardParams, false)
						.addStatement("return new $T(\n\t$L,\n\t$T.of($L),\n\t$T.of($L),\n\t$T.of($L))",
								starterLineUpClass, goalieParams.get(0).name, // goalie1
								List.class, defendersList, List.class, midfieldersList, List.class, forwardsList)
						.build())
				.returns(starterLineUpClass).build();

	    // 8. builds the Builder class
		TypeSpec.Builder concreteBuilder = TypeSpec.classBuilder(builderClassName.simpleName())
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addSuperinterface(readyForGk)
				.addSuperinterface(readyForDef)
				.addSuperinterface(readyForMid)
				.addSuperinterface(readyForFwd)
				.addMethod(withGoalkeeperConcrete)
				.addMethod(withDefendersConcrete)
				.addMethod(withMidfieldersConcrete)
				.addMethod(withForwardsConcrete)
				.addFields(Stream.concat(
								Stream.concat(goalieParams.stream(), defenderParams.stream()),
								midfielderParams.stream())
						.map(param -> FieldSpec.builder(param.type, param.name, Modifier.PRIVATE))
						.map(FieldSpec.Builder::build).collect(Collectors.toList()));		
	    
	    // 9. builds the final top-level class
	    TypeSpec schemeClass = TypeSpec.classBuilder(generatedClassString)
	            .addModifiers(Modifier.PUBLIC)
	            .superclass(schemeBaseClass)
	            .addField(FieldSpec.builder(generatedClassName, "INSTANCE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
	                .initializer("new $T()", generatedClassName)
	                .build())
	            .addMethod(MethodSpec.methodBuilder(starterLineUpClass.simpleName())
	                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
	                .returns(readyForGk)
	                .addStatement("return new $L()", builderClassName.simpleName())
	                .build())
	            .addType(stepsClass)
	            .addType(concreteBuilder.build())
	            .build();

	    // 10. write the file to disk
	    JavaFile javaFile = JavaFile.builder(targetPackage, schemeClass).build();
	    JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(targetPackage + "." + generatedClassString);
	    try (Writer writer = fileObject.openWriter()) {
	        javaFile.writeTo(writer);
	    }
	}

	/**
	 * constructs a {@linkplain CodeBlock.Builder} containing the body of the
	 * concrete Step Builder's implementor methods, consisting of
	 * <ol>
	 * <li>null checks on parameters and, optionally, assignments to fields
	 * <li>duplicate checks on parameters and throwing statement
	 * </ol>
	 * 
	 * @param params      the method's formal parameters
	 * @param assignments {@code true} to insert assignments to fields
	 * @return said {@linkplain CodeBlock.Builder}, not yet built
	 */
	private CodeBlock.Builder implementorBody(List<ParameterSpec> params, boolean assignments) {
	    CodeBlock.Builder code = CodeBlock.builder();
	    
	    // 1. Add null checks for all parameters
	    params.forEach(param -> {
	    	if (assignments)
	    		code.addStatement("this.$N = $T.requireNonNull($N)", param.name, Objects.class, param.name);
	    	else
	    		code.addStatement("$T.requireNonNull($N)", Objects.class, param.name);
	    });

	    // 2. Add duplicate checks if there's more than one parameter
	    if (params.size() > 1) {
	        // Use a StringJoiner for clean "a.equals(b) || b.equals(c)" logic
	        StringJoiner condition = new StringJoiner(" ||\n");
	        for (int i = 0; i < params.size(); i++) {
	            for (int j = i + 1; j < params.size(); j++) {
	                condition.add(String.format("%s.equals(%s)", params.get(i).name, params.get(j).name));
	            }
	        }
	        code.beginControlFlow("if ($L)", condition.toString());
	        code.addStatement("throw new $T()", IllegalArgumentException.class);
	        code.endControlFlow();
	    }

	    return code;
	}
	
	/**
	 * generates a parameter list such as
	 * 
	 * <pre>
	 * String string1, String string2, String string3
	 * </pre>
	 * 
	 * @param type     the parameters' type
	 * @param baseName the base name
	 * @param count    the last integer to be reached, starting from 1, inclusive
	 * @return the {@code List<ParameterSpec>} so construed
	 */
	private List<ParameterSpec> generateParameters(TypeName type, String baseName, int count) {
		return IntStream.rangeClosed(1, count)
				.mapToObj(i -> ParameterSpec.builder(type, baseName + i))
				.map(Builder::build)
				.collect(Collectors.toList());
	}
}