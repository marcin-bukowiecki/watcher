package com.watcher.utils;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marcin Bukowiecki
 */
public class JavaASTUtils {

    public static boolean isReferenceAccess(Node node) {
        if (node instanceof NameExpr) {
            return true;
        }
        return node instanceof FieldAccessExpr;
    }

    public static List<Node> extractReferenceAccessNodes(Node node) {
        final ArrayList<Node> acc = new ArrayList<>();
        extractReferenceAccessNodes(node, acc);
        return acc;
    }

    public static void extractReferenceAccessNodes(Node node, List<Node> acc) {
        if (isReferenceAccess(node)) {
            acc.add(node);
            return;
        }
        if (node instanceof ThrowStmt) {
            acc.add(node);
            return;
        }
        if (node instanceof ForeachStmt) {
            final ForeachStmt forStmt = (ForeachStmt) node;

            if (forStmt.getVariable() != null) {
                if (isReferenceAccess(forStmt.getVariable())) {
                    acc.add(forStmt.getVariable());
                } else {
                    extractReferenceAccessNodes(forStmt.getVariable(), acc);
                }
            }

            if (forStmt.getIterable() != null) {
                if (isReferenceAccess(forStmt.getIterable())) {
                    acc.add(forStmt.getIterable());
                } else {
                    extractReferenceAccessNodes(forStmt.getIterable(), acc);
                }
            }

            if (forStmt.getBody() != null) {
                if (isReferenceAccess(forStmt.getBody())) {
                    acc.add(forStmt.getBody());
                } else {
                    extractReferenceAccessNodes(forStmt.getBody(), acc);
                }
            }

            return;
        }

        if (node instanceof VariableDeclarationExpr) {
            final VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) node;

            return;
        }

        if (node instanceof ObjectCreationExpr) {
            final ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) node;

            if (objectCreationExpr.getArgs() != null) {
                for (Node child : objectCreationExpr.getArgs()) {
                    if (isReferenceAccess(child)) {
                        acc.add(child);
                    } else {
                        extractReferenceAccessNodes(child, acc);
                    }
                }
            }

            //acc.add(node);
            return;
        }

        if (node instanceof ForStmt) {
            final ForStmt forStmt = (ForStmt) node;

            if (forStmt.getInit() != null) {
                for (Node child : forStmt.getInit()) {
                    if (isReferenceAccess(child)) {
                        acc.add(child);
                    } else {
                        extractReferenceAccessNodes(child, acc);
                    }
                }
            }

            if (forStmt.getCompare() != null) {
                if (isReferenceAccess(forStmt.getCompare())) {
                    acc.add(forStmt.getCompare());
                } else {
                    extractReferenceAccessNodes(forStmt.getCompare(), acc);
                }
            }

            if (forStmt.getUpdate() != null) {
                for (Node child : forStmt.getUpdate()) {
                    if (isReferenceAccess(child)) {
                        acc.add(child);
                    } else {
                        extractReferenceAccessNodes(child, acc);
                    }
                }
            }

            if (forStmt.getBody() != null) {
                if (isReferenceAccess(forStmt.getBody())) {
                    acc.add(forStmt.getBody());
                } else {
                    extractReferenceAccessNodes(forStmt.getBody(), acc);
                }
            }

            return;
        }
        if (node instanceof MethodCallExpr) {
            final MethodCallExpr methodCall = (MethodCallExpr) node;

            if (methodCall.getChildrenNodes() != null) {
                for (Node child : methodCall.getChildrenNodes()) {
                    if (isReferenceAccess(child)) {
                        acc.add(child);
                    } else {
                        extractReferenceAccessNodes(child, acc);
                    }
                }
            }

           if (methodCall.getArgs() != null) {
                for (Node child : methodCall.getArgs()) {
                    if (isReferenceAccess(child)) {
                        acc.add(child);
                    } else {
                        extractReferenceAccessNodes(child, acc);
                    }
                }
            }

            acc.add(node);
            return;
        }
        for (Node child : node.getChildrenNodes()) {
            if (isReferenceAccess(child)) {
                acc.add(child);
            } else {
                extractReferenceAccessNodes(child, acc);
            }
        }
    }
}
