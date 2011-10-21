package org.dyndns.delphyne.couchdb.internal

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase=CompilePhase.CANONICALIZATION)
class DocumentASTTransformation extends AbstractASTTransformation {
    public void visit(ASTNode[] nodes, SourceUnit source) {
    }
}
