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
	    String className = scheme.className();
	    String targetPackage = "domainModel.scheme"; 
	    
	    ClassName generatedClassType = ClassName.get(targetPackage, className);
	    String builderClassName = "StarterLineUpBuilder" + className.substring(6);
	    String stepsClassName = builderClassName + "Steps";

	    // FIX: Ensure your domain model classes have their full package paths
	    String domainPackage = "domainModel";
	    ClassName schemeBaseClass = ClassName.get(domainPackage, "Scheme");
	    ClassName starterLineUpClass = ClassName.get(domainPackage, "StarterLineUp");
	    ClassName gkClass = ClassName.get(domainPackage + ".Player", "Goalkeeper");
	    ClassName defClass = ClassName.get(domainPackage + ".Player", "Defender");
	    ClassName midClass = ClassName.get(domainPackage + ".Player", "Midfielder");
	    ClassName fwdClass = ClassName.get(domainPackage + ".Player", "Forward");
	    
	    // Step interfaces
	    TypeName readyForGk = ClassName.get("", stepsClassName, "ReadyForGoalkeeper");
	    TypeName readyForDef = ClassName.get("", stepsClassName, "ReadyForDefenders");
	    TypeName readyForMid = ClassName.get("", stepsClassName, "ReadyForMidfielders");
	    TypeName readyForFwd = ClassName.get("", stepsClassName, "ReadyForForwards");
	    
	    // Generate parameter lists once for reuse
	    List<ParameterSpec> goalieParams = generateParameters(gkClass, "goalie", 1);
	    List<ParameterSpec> defenderParams = generateParameters(defClass, "defender", scheme.defenders());
	    List<ParameterSpec> midfielderParams = generateParameters(midClass, "midfielder", scheme.midfielders());
	    List<ParameterSpec> forwardParams = generateParameters(fwdClass, "forward", scheme.forwards());

	    // --- Build the abstract interface methods ---
	    MethodSpec withGoalieAbstract = MethodSpec.methodBuilder("withGoalkeeper")
	    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
	    		.addParameters(goalieParams)
	    		.returns(readyForDef)
	    		.build();
	    MethodSpec withDefendersAbstract = MethodSpec.methodBuilder("withDefenders")
	    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
	    		.addParameters(defenderParams)
	    		.returns(readyForMid)
	    		.build();
	    MethodSpec withMidfieldersAbstract = MethodSpec.methodBuilder("withMidfielders")
	    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
	    		.addParameters(midfielderParams)
	    		.returns(readyForFwd)
	    		.build();
	    MethodSpec withForwardsAbstract = MethodSpec.methodBuilder("withForwards")
	    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
	    		.addParameters(forwardParams)
	    		.returns(starterLineUpClass)
	    		.build();

	    // --- Build Step Builder Interfaces ---
	    // FIX Bug 1: Use the correct names for each interface.
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
	    
	    // --- Build the Steps container class ---
	    TypeSpec stepsClass = TypeSpec.classBuilder(stepsClassName)
	            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.ABSTRACT)
	            .addType(readyForGkInterface)
	            .addType(readyForDefInterface)
	            .addType(readyForMidInterface)
	            .addType(readyForForwInterface)
	            .build();
	    
	    // --- Build the concrete implementation methods ---
	    // FIX Bug 2: Correct the method name for withGoalkeeper.
	    MethodSpec withGoalkeeperConcrete = MethodSpec.methodBuilder("withGoalkeeper")
	            .addAnnotation(Override.class)
	            .addModifiers(Modifier.PUBLIC)
	            .addParameters(goalieParams)
	            .addCode(implementorBody(goalieParams, true).build())
	            .addStatement("return this")
	            .returns(readyForDef)
	            .build();
	    
	    MethodSpec withDefendersConcrete = MethodSpec.methodBuilder("withDefenders")
	            .addAnnotation(Override.class)
	            .addModifiers(Modifier.PUBLIC)
	            .addParameters(defenderParams)
	            .addCode(implementorBody(defenderParams, true).build())
	            .addStatement("return this")
	            .returns(readyForMid)
	            .build();
	    
	    MethodSpec withMidfieldersConcrete = MethodSpec.methodBuilder("withMidfielders")
	            .addAnnotation(Override.class)
	            .addModifiers(Modifier.PUBLIC)
	            .addParameters(midfielderParams)
	            .addCode(implementorBody(midfielderParams, true).build())
	            .addStatement("return this")
	            .returns(readyForFwd)
	            .build();
	    
	    // FIX Bug 4: Implement the final return statement.
	    // Create the 'List.of(defender1, defender2, ...)' arguments
	    String defendersList = defenderParams.stream().map(p -> p.name).collect(Collectors.joining(", "));
	    String midfieldersList = midfielderParams.stream().map(p -> p.name).collect(Collectors.joining(", "));
	    String forwardsList = forwardParams.stream().map(p -> p.name).collect(Collectors.joining(", "));

		MethodSpec withForwardsConcrete = MethodSpec.methodBuilder("withForwards")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameters(forwardParams)
				.addCode(implementorBody(forwardParams, false)
						.addStatement("return new $T(\n\t$L,\n\t$T.of($L),\n\t$T.of($L),\n\t$T.of($L))",
								starterLineUpClass, goalieParams.get(0).name, // goalie1
								List.class, defendersList, List.class, midfieldersList, List.class, forwardsList)
						.build())
				.returns(starterLineUpClass).build();

	    // --- Build the Builder implementation class ---
	    TypeSpec.Builder concreteBuilder = TypeSpec.classBuilder(builderClassName)
	            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
	            .addSuperinterface(readyForGk)
	            .addSuperinterface(readyForDef)
	            .addSuperinterface(readyForMid)
	            .addSuperinterface(readyForFwd)
	            .addMethod(withGoalkeeperConcrete)
	            .addMethod(withDefendersConcrete)
	            .addMethod(withMidfieldersConcrete)
	            .addMethod(withForwardsConcrete);

	    // Add fields to the builder
	    goalieParams.forEach(param -> concreteBuilder.addField(param.type, param.name, Modifier.PRIVATE));
	    defenderParams.forEach(param -> concreteBuilder.addField(param.type, param.name, Modifier.PRIVATE));
	    midfielderParams.forEach(param -> concreteBuilder.addField(param.type, param.name, Modifier.PRIVATE));
	    
	    // --- Build the final top-level class ---
	    TypeSpec schemeClass = TypeSpec.classBuilder(className)
	            .addModifiers(Modifier.PUBLIC)
	            .superclass(schemeBaseClass)
	            .addField(FieldSpec.builder(generatedClassType, "INSTANCE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
	                .initializer("new $T()", generatedClassType)
	                .build())
	            .addMethod(MethodSpec.methodBuilder("create")
	                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
	                .returns(readyForGk)
	                .addStatement("return new $L()", builderClassName)
	                .build())
	            .addType(stepsClass)
	            .addType(concreteBuilder.build())
	            .build();

	    // --- Write the file ---
	    JavaFile javaFile = JavaFile.builder(targetPackage, schemeClass).build();
	    JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(targetPackage + "." + className);
	    try (Writer writer = fileObject.openWriter()) {
	        javaFile.writeTo(writer);
	    }
	}

	// FIX Bug 3: A completely refactored and robust helper method.
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
	
	// Helper method inside your processor
	private List<ParameterSpec> generateParameters(TypeName type, String baseName, int count) {
		return IntStream.rangeClosed(1, count)
				.mapToObj(i -> ParameterSpec.builder(type, baseName + i))
				.map(Builder::build)
				.collect(Collectors.toList());
	}
}