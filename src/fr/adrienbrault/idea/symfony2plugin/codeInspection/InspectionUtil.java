package fr.adrienbrault.idea.symfony2plugin.codeInspection;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import fr.adrienbrault.idea.symfony2plugin.codeInspection.quickfix.CreateMethodQuickFix;
import fr.adrienbrault.idea.symfony2plugin.routing.Route;
import fr.adrienbrault.idea.symfony2plugin.routing.RouteHelper;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class InspectionUtil {

    public static void inspectController(@NotNull PsiElement psiElement, @NotNull String controllerName, @NotNull ProblemsHolder holder, final @Nullable String routeName) {

        int lastPos = controllerName.lastIndexOf(":") + 1;
        final String actionName = controllerName.substring(lastPos) + "Action";

        List<PsiElement> psiElements = Arrays.asList(RouteHelper.getMethodsOnControllerShortcut(psiElement.getProject(), controllerName));
        if(psiElements.size() > 0) {
            return;
        }

        PhpClass phpClass = RouteHelper.getControllerClassOnShortcut(psiElement.getProject(), controllerName);
        if(phpClass == null) {
            return;
        }

        final Project project = phpClass.getProject();
        holder.registerProblem(psiElement, "Create Method", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new CreateMethodQuickFix(phpClass, actionName, new CreateMethodQuickFix.InsertStringInterface() {
            @NotNull
            @Override
            public StringBuilder getStringBuilder() {

                String parameters = "";
                if(routeName != null) {
                    Route route = RouteHelper.getRoute(project, routeName);

                    if(route != null) {
                        Set<String> vars = route.getVariables();
                        if(vars.size() > 0) {
                            List<String> varsDollar = new ArrayList<String>();
                            for(String var: vars) {
                                varsDollar.add("$" + var);
                            }

                            parameters = StringUtils.join(varsDollar, ", ");
                        }
                    }
                }

                return new StringBuilder()
                    .append("public function ")
                    .append(actionName)
                    .append("(")
                    .append(parameters)
                    .append(")\n {\n}\n\n");
            }
        }));

    }

}
