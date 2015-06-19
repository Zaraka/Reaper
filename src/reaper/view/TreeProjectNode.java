/*
 * The MIT License
 *
 * Copyright 2015 Reaper.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package reaper.view;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TreeItem;
import reaper.model.Project;

/**
 *
 * @author nikita.vanku
 */
public class TreeProjectNode {

    private Project project;
    private ProjectNode nodeType;
    private List<TreeItem<TreeProjectNode>> children;

    public TreeProjectNode(Project proj, ProjectNode type) {
        project = proj;
        nodeType = type;
    }

    public TreeProjectNode(Project proj) {
        project = proj;
        nodeType = ProjectNode.PROJECT;

        children = new ArrayList<>();
        children.add(new TreeItem<>(new TreeProjectNode(proj, ProjectNode.VIEW)));
        children.add(new TreeItem<>(new TreeProjectNode(proj, ProjectNode.STATISTICS)));
    }

    public ProjectNode getNodeType() {
        return nodeType;
    }

    public Project getProject() {
        return project;
    }

    public void setNodeType(ProjectNode nodeType) {
        this.nodeType = nodeType;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<TreeItem<TreeProjectNode>> getChildren() {
        return children;
    }

    public Boolean isLeaf() {
        if (nodeType.equals(ProjectNode.PROJECT) || nodeType.equals(ProjectNode.ROOT)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        switch (nodeType) {
            case PROJECT:
                return project.getName();
            case ROOT:
                return "root";
            case STATISTICS:
                return "Statistics";
            case VIEW:
                return "View";
            default:
                return "undefined";
        }
    }
}
