package com.siyeh.ig.jdk;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiReferenceExpression;
import com.siyeh.ig.*;
import com.siyeh.ig.psiutils.ExpectedTypeUtils;
import com.siyeh.ig.psiutils.ClassUtils;

import java.util.HashMap;
import java.util.Map;

public class AutoBoxingInspection extends ExpressionInspection {
    private static final Map s_boxingClasses = new HashMap(8);
    private final AutoBoxingFix fix = new AutoBoxingFix();

    static {
        s_boxingClasses.put("int", "Integer");
        s_boxingClasses.put("short", "Short");
        s_boxingClasses.put("boolean", "Boolean");
        s_boxingClasses.put("long", "Long");
        s_boxingClasses.put("byte", "Byte");
        s_boxingClasses.put("float", "Float");
        s_boxingClasses.put("double", "Double");
        s_boxingClasses.put("long", "Long");
    }

    public String getDisplayName() {
        return "Auto-boxing";
    }

    public String getGroupDisplayName() {
        return GroupNames.JDK_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return "Auto-boxing #ref #loc";
    }

    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager, boolean onTheFly) {
        return new AutoBoxingVisitor(this, inspectionManager, onTheFly);
    }

    public InspectionGadgetsFix buildFix(PsiElement location) {
        return fix;
    }

    private static class AutoBoxingFix extends InspectionGadgetsFix {
        public String getName() {
            return "Make boxing explicit";
        }

        public void applyFix(Project project, ProblemDescriptor descriptor) {
            if(isQuickFixOnReadOnlyFile(project, descriptor)) return;
            final PsiExpression expression = (PsiExpression) descriptor.getPsiElement();
            final PsiType expectedType = ExpectedTypeUtils.findExpectedType(expression);
            final String newExpression;
            if (expectedType.equals(PsiType.BOOLEAN)) {
                newExpression = "Boolean.valueOf(" + expression.getText() + ')';
            } else if (s_boxingClasses.containsValue(expectedType.getPresentableText())) {
                final String classToConstruct = expectedType.getPresentableText();
                newExpression = "new " + classToConstruct + '(' + expression.getText() + ')';
            } else {
                final String classToConstruct = (String) s_boxingClasses.get(expression.getType().getPresentableText());
                newExpression = "new " + classToConstruct + '(' + expression.getText() + ')';
            }
            replaceExpression(project, expression, newExpression);
        }
    }

    private static class AutoBoxingVisitor extends BaseInspectionVisitor {
        private AutoBoxingVisitor(BaseInspection inspection, InspectionManager inspectionManager, boolean isOnTheFly) {
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitReferenceExpression(PsiReferenceExpression expression){
            super.visitReferenceExpression(expression);
            checkExpression(expression);
        }

        public void visitExpression(PsiExpression expression) {
            super.visitExpression(expression);
            checkExpression(expression);
        }

        private void checkExpression(PsiExpression expression)
        {

            final PsiType expressionType = expression.getType();
            if(expressionType == null){
                return;
            }
            if(!ClassUtils.isPrimitive(expressionType)){
                return;
            }
            final PsiType expectedType =
                    ExpectedTypeUtils.findExpectedType(expression);
            if(expectedType == null){
                return;
            }

            if(ClassUtils.isPrimitive(expectedType)){
                return;
            }
            registerError(expression);
        }

    }

}
