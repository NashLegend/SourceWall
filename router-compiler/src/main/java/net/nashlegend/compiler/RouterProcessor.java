package net.nashlegend.compiler;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class RouterProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private String dic = "net.nashlegend.router";

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
        messager = env.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(net.nashlegend.router.annotations.ViewRoute.class.getCanonicalName());
        types.add(net.nashlegend.router.annotations.ActionRoute.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> ViewElements = roundEnv.getElementsAnnotatedWith(net.nashlegend.router.annotations.ViewRoute.class);
        Set<? extends Element> ActionElements = roundEnv.getElementsAnnotatedWith(net.nashlegend.router.annotations.ActionRoute.class);
        if (ViewElements.size() > 0 || ActionElements.size() > 0) {
            MethodSpec.Builder trackMethod = MethodSpec.methodBuilder("trackAllRoutine")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                    .addStatement("java.util.Map<String,String> transfer = null")
                    .addStatement(dic + ".ExtraTypes extraTypes")
                    .addCode("\n");

            for (Element activity : ViewElements) {
                if (!SuperficialValidation.validateElement(activity)) {
                    continue;
                }
                if (activity.getKind() != ElementKind.CLASS) {
                    error("Router can only apply on class");
                }
                net.nashlegend.router.annotations.ViewRoute router = activity.getAnnotation(net.nashlegend.router.annotations.ViewRoute.class);
                viewRouterExtra(router, trackMethod);
                for (String format : router.value()) {
                    trackMethod.addStatement(dic + ".Router.ViewRoute($S, $T.class, extraTypes)", format, ClassName.get((TypeElement) activity));
                }
                trackMethod.addCode("\n");
            }
            for (Element action : ActionElements) {
                if (!SuperficialValidation.validateElement(action)) {
                    continue;
                }
                if (action.getKind() != ElementKind.CLASS) {
                    error("Router can only apply on class");
                }
                net.nashlegend.router.annotations.ActionRoute router = action.getAnnotation(net.nashlegend.router.annotations.ActionRoute.class);
                actionRouterExtra(router, trackMethod);
                for (String format : router.value()) {
                    trackMethod.addStatement(dic + ".Router.ActionRoute($S, $T.class, extraTypes)", format, ClassName.get((TypeElement) action));
                }
                trackMethod.addCode("\n");
            }
            trackMethod.addStatement(dic + ".Router.sort()");

            TypeSpec routerMapping = TypeSpec.classBuilder("Tracks")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(trackMethod.build())
                    .build();
            messager.printMessage(Diagnostic.Kind.WARNING, "Muhaha");
            try {
                JavaFile.builder(dic, routerMapping)
                        .addFileComment("Generated code from route. Do not modify!")
                        .build()
                        .writeTo(filer);
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage() + "~~~");
            }
        }
        return true;
    }

    private void viewRouterExtra(net.nashlegend.router.annotations.ViewRoute router, MethodSpec.Builder trackMethod) {
        String[] transfer = router.transfer();
        if (transfer != null && transfer.length > 0 && !"".equals(transfer[0])) {
            trackMethod.addStatement("transfer = new java.util.HashMap<String, String>()");
            for (String s : transfer) {
                String[] components = s.split("=>");
                if (components.length != 2) {
                    error("transfer `" + s + "` not match a=>b format");
                    break;
                }
                trackMethod.addStatement("transfer.put($S, $S)", components[0], components[1]);
            }
        } else {
            trackMethod.addStatement("transfer = null");
        }

        trackMethod.addStatement("extraTypes = new " + dic + ".ExtraTypes()");
        trackMethod.addStatement("extraTypes.setTransfer(transfer)");
        String extras = join(router.intExtra());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setIntExtra($S.split(\",\"))", extras);
        }
        extras = join(router.longExtra());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setLongExtra($S.split(\",\"))", extras);
        }
        extras = join(router.boolExtra());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setBoolExtra($S.split(\",\"))", extras);
        }
        extras = join(router.shortExtra());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setShortExtra($S.split(\",\"))", extras);
        }
        extras = join(router.floatExtra());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setFloatExtra($S.split(\",\"))", extras);
        }
        extras = join(router.doubleExtra());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setDoubleExtra($S.split(\",\"))", extras);
        }
        extras = join(router.required());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setRequired($S.split(\",\"))", extras);
        }
    }

    private void actionRouterExtra(net.nashlegend.router.annotations.ActionRoute router, MethodSpec.Builder trackMethod) {
        String[] transfer = router.transfer();
        if (transfer != null && transfer.length > 0 && !"".equals(transfer[0])) {
            trackMethod.addStatement("transfer = new java.util.HashMap<String, String>()");
            for (String s : transfer) {
                String[] components = s.split("=>");
                if (components.length != 2) {
                    error("transfer `" + s + "` not match a=>b format");
                    break;
                }
                trackMethod.addStatement("transfer.put($S, $S)", components[0], components[1]);
            }
        } else {
            trackMethod.addStatement("transfer = null");
        }

        trackMethod.addStatement("extraTypes = new " + dic + ".ExtraTypes()");
        trackMethod.addStatement("extraTypes.setTransfer(transfer)");
        String extras = join(router.intExtra());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setIntExtra($S.split(\",\"))", extras);
        }
        extras = join(router.longExtra());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setLongExtra($S.split(\",\"))", extras);
        }
        extras = join(router.boolExtra());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setBoolExtra($S.split(\",\"))", extras);
        }
        extras = join(router.shortExtra());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setShortExtra($S.split(\",\"))", extras);
        }
        extras = join(router.floatExtra());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setFloatExtra($S.split(\",\"))", extras);
        }
        extras = join(router.doubleExtra());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setDoubleExtra($S.split(\",\"))", extras);
        }
        extras = join(router.required());
        if (extras.length() > 0) {
            trackMethod.addStatement("extraTypes.setRequired($S.split(\",\"))", extras);
        }
    }

    private String join(String[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        if (args.length == 1) {
            return args[0];
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length - 1; i++) {
            sb.append(args[i]).append(",");
        }
        sb.append(args[args.length - 1]);
        return sb.toString();
    }

    private void error(String error) {
        messager.printMessage(Diagnostic.Kind.ERROR, error);
    }

}
