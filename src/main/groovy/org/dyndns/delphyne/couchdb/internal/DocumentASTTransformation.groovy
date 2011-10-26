package org.dyndns.delphyne.couchdb.internal

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.dyndns.delphyne.couchdb.Id
import org.dyndns.delphyne.couchdb.Repository

import groovy.util.logging.Log

@Log
@GroovyASTTransformation(phase=CompilePhase.CANONICALIZATION)
class DocumentASTTransformation extends AbstractASTTransformation {

    AstBuilder builder = new AstBuilder()

    void visit(ASTNode[] nodes, SourceUnit source) {
        if (!nodes || !(nodes[0] instanceof AnnotationNode) || !(nodes.size() >= 2) || !(nodes[1] instanceof ClassNode)) {
            return
        }

        ClassNode classNode = (ClassNode) nodes[1]

        configureId(classNode, source)
        configureRev(classNode, source)
        mixinRepository(classNode, source)
    }

    void configureId(ClassNode clazz, SourceUnit source) {
        FieldNode existingIdField = clazz.getDeclaredField('_id')
        if (!existingIdField) {
            FieldNode keyField = clazz.fields.find { FieldNode field ->
                field.getAnnotations(ClassHelper.make(Id))?.getAt(0)
            }

            if(keyField) {
                clazz.addMethod(
                                                'get_id',
                                                ACC_PUBLIC,
                                                ClassHelper.STRING_TYPE,
                                                Parameter.EMPTY_ARRAY,
                                                ClassHelper.EMPTY_TYPE_ARRAY,
                                                builder.buildFromSpec { expression { variable "${keyField.name}" } }[0])
                clazz.addMethod(
                                                'set_id',
                                                ACC_PUBLIC,
                                                ClassHelper.VOID_TYPE,
                                                [
                                                    new Parameter(ClassHelper.STRING_TYPE, '_id')
                                                ]
                                                as Parameter[],
                                                ClassHelper.EMPTY_TYPE_ARRAY,
                                                builder.buildFromSpec {
                                                    expression {
                                                        binary {
                                                            variable "${keyField.name}"
                                                            token '='
                                                            variable '_id'
                                                        }
                                                    }
                                                }[0])
            } else {
                clazz.addProperty('_id', ACC_PUBLIC, ClassHelper.STRING_TYPE, null, null, null)
            }
        }
    }

    void configureRev(ClassNode clazz, SourceUnit source) {
        clazz.addProperty('_rev', ACC_PUBLIC, ClassHelper.STRING_TYPE, null, null, null)
    }

    void mixinRepository(ClassNode clazz, SourceUnit source) {
        MethodNode clinit = clazz.getDeclaredMethod('<clinit>', Parameter.EMPTY_ARRAY)
        if (!clinit) {
            clinit = new MethodNode(
                                            '<clinit>',
                                            ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
                                            ClassHelper.VOID_TYPE,
                                            Parameter.EMPTY_ARRAY,
                                            ClassHelper.EMPTY_TYPE_ARRAY,
                                            new BlockStatement()
                                            )
            clazz.addMethod(clinit)
        }
        BlockStatement code = clinit.code
        code.addStatements(builder.buildFromSpec{
            expression {
                methodCall {
                    variable 'this'
                    constant 'mixin'
                    argumentList { classExpression Repository }
                }
            }
        })
    }
}
