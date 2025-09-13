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
		String basePackage = "com.fantacalcio.app.model";
		ClassName schemeClassType = ClassName.get(basePackage + ".scheme", className);
		String builderClassName = "StarterLineUpBuilder" + className.substring(6); // e.g., StarterLineUpBuilder433
		String stepsClassName = builderClassName + "Steps";

		// Assume these classes exist in your football-app module
		ClassName schemeBaseClass = ClassName.get("domainModel", "Scheme");
		ClassName starterLineUpClass = ClassName.get("domainModel", "StarterLineUp"); // TODO update package
		ClassName gkClass = ClassName.get("domainModel.Player", "Goalkeeper");
		ClassName defClass = ClassName.get("domainModel.Player", "Defender");
		ClassName midClass = ClassName.get("domainModel.Player", "Midfielder");
		ClassName fwdClass = ClassName.get("domainModel.Player", "Forward");
		
		// step interfaces
		TypeName readyForGk = ClassName.get("", stepsClassName, "ReadyForGoalkeeper");
		TypeName readyForDef = ClassName.get("", stepsClassName, "ReadyForDefenders");
		TypeName readyForMid = ClassName.get("", stepsClassName, "ReadyForMidfielders");
		TypeName readyForFwd = ClassName.get("", stepsClassName, "ReadyForForwards");
		
		// 2) building methods for Step Interfaces
		List<ParameterSpec> goalieParams = generateParameters(gkClass, "goalkeeper", 1);
		List<ParameterSpec> defenderParams = generateParameters(defClass, "defender", scheme.defenders());
		List<ParameterSpec> midfielderParams = generateParameters(midClass, "midfielder", scheme.midfielders());
		List<ParameterSpec> forwardParams = generateParameters(fwdClass, "forward", scheme.forwards());

		// --- Build the interface methods using the generated parameters ---
		MethodSpec withGoalieAbstract = MethodSpec.methodBuilder("withGoalkeeper")
			    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
			    .addParameters(goalieParams)
				.returns(readyForDef)
			    .build();
		
		MethodSpec withDefendersAbstract = MethodSpec.methodBuilder("withDefenders")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addParameters(defenderParams) // <-- REUSE
				.returns(readyForMid)
				.build();

		MethodSpec withMidfieldersAbstract = MethodSpec.methodBuilder("withMidfielders")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addParameters(midfielderParams) // <-- REUSE
				.returns(readyForFwd)
				.build();
		
		MethodSpec withForwardsAbstract = MethodSpec.methodBuilder("withForwards")
			    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
			    .addParameters(forwardParams) // <-- REUSE
			    .returns(starterLineUpClass)
			    .build();

		// --- Build Step Builder Interfaces ---
		TypeSpec readyForGkInterface = TypeSpec.interfaceBuilder("ReadyForGoalkeeper")
				.addMethod(withGoalieAbstract)
				.build();
		
		TypeSpec readyForDefInterface = TypeSpec.interfaceBuilder("ReadyForGoalkeeper")
				.addMethod(withDefendersAbstract)
				.build();
		
		TypeSpec readyForMidInterface = TypeSpec.interfaceBuilder("ReadyForGoalkeeper")
				.addMethod(withMidfieldersAbstract)
				.build();
		
		TypeSpec readyForForwInterface = TypeSpec.interfaceBuilder("ReadyForGoalkeeper")
				.addMethod(withForwardsAbstract)
				.build();
		
		// 3. Build the Steps container class
		TypeSpec stepsClass = TypeSpec.classBuilder(stepsClassName)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.ABSTRACT)
				.addType(readyForGkInterface)
				.addType(readyForDefInterface)
				.addType(readyForMidInterface)
				.addType(readyForForwInterface)
				.build();
		
		// 3) members of concrete Builder
		MethodSpec withGoalkeeperConcrete = MethodSpec.methodBuilder("withDefenders")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameters(goalieParams)
				.addCode(implementorBody(goalieParams)
						.addStatement("return this")
						.build())
				.returns(readyForDef)
				.build();
		
		MethodSpec withDefendersConcrete = MethodSpec.methodBuilder("withDefenders")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameters(defenderParams)
				.addCode(implementorBody(defenderParams)
						.addStatement("return this")
						.build())
				.returns(readyForMid)
				.build();
		
		MethodSpec withMidfieldersConcrete = MethodSpec.methodBuilder("withMidfielders")
			    .addAnnotation(Override.class)
			    .addModifiers(Modifier.PUBLIC)
			    .addParameters(midfielderParams)
			    .addCode(implementorBody(midfielderParams)
			    		.addStatement("return this")
						.build())
			    .returns(readyForFwd)
			    .build();
		
		MethodSpec withForwardsConcrete = MethodSpec.methodBuilder("withForwards")
			    .addAnnotation(Override.class)
			    .addModifiers(Modifier.PUBLIC)
			    .addParameters(forwardParams)
			    .addCode(implementorBody(forwardParams)
			    		.addStatement("return this")	// TODO call actual StarterLineUp constructor
						.build())
			    .returns(starterLineUpClass)
			    .build();

		// 4. Build the Builder implementation class
		TypeSpec.Builder concreteBuilder = TypeSpec.classBuilder(builderClassName)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC).addSuperinterface(readyForGk)
				.addSuperinterface(readyForGk)
				.addSuperinterface(readyForDef)
				.addSuperinterface(readyForMid)
				.addSuperinterface(readyForFwd)
				.addMethod(withGoalkeeperConcrete)
				.addMethod(withDefendersConcrete)
				.addMethod(withMidfieldersConcrete)
				.addMethod(withForwardsConcrete);

		// Add fields to the builder
		goalieParams.forEach(param -> concreteBuilder.addField(gkClass, param.name, Modifier.PRIVATE));
		defenderParams.forEach(param -> concreteBuilder.addField(param.type, param.name, Modifier.PRIVATE));
		midfielderParams.forEach(param -> concreteBuilder.addField(param.type, param.name, Modifier.PRIVATE));
		forwardParams.forEach(param -> concreteBuilder.addField(param.type, param.name, Modifier.PRIVATE));
		
		// 5. Build the final top-level class
		TypeSpec schemeClass = TypeSpec.classBuilder(className)
		    .addModifiers(Modifier.PUBLIC)
		    .superclass(schemeBaseClass)
		    // Add INSTANCE field
		    .addField(FieldSpec.builder(schemeClassType, "INSTANCE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
		        .initializer("new $T()", schemeClassType)
		        .build())
		    // Add create() method
		    .addMethod(MethodSpec.methodBuilder("create")
		        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
		        .returns(readyForGk)
		        .addStatement("return new $L()", builderClassName)
		        .build())
		    // Add the nested classes
		    .addType(stepsClass)
		    .addType(concreteBuilder.build())
		    .build();

		// --- Write the file ---
		JavaFile javaFile = JavaFile.builder("domainModel", schemeClass).build();

		JavaFileObject fileObject = processingEnv.getFiler()
				.createSourceFile("domainModel." + className);   // final dot is CORRECT!!
		try (Writer writer = fileObject.openWriter()) {
			System.out.println("about to write file!!");
			javaFile.writeTo(writer);
		}
	}

	private CodeBlock.Builder implementorBody(List<ParameterSpec> params) {
		CodeBlock.Builder code = CodeBlock.builder();
		params.forEach(param -> code.addStatement(
				"this.$N = $T.requireNonNull($N)", param.name, Objects.class, param.name));

		// Add the method body (duplicate checks - a bit more complex)
		if (params.size() > 1) {
		    code.beginControlFlow("if (");
		    for (int i = 0; i < params.size(); i++) {
		        for (int j = i + 1; j < params.size(); j++) {
		            code.add("$N.equals($N)", params.get(i).name, params.get(j).name);
		            if (i != params.size() -2)	// the last pair has i == size - 2
		            	code.add(" || \\n");
		        }
		    }
//		    String checkStr = duplicateCheck.build().toString();
//		    checkStr = checkStr.substring(0, checkStr.lastIndexOf(" ||"));
		    code.add(")\n");
		    code.addStatement("\tthrow new $T()", IllegalArgumentException.class);
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