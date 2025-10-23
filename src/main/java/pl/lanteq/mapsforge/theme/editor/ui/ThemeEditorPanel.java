package pl.lanteq.mapsforge.theme.editor.ui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import pl.lanteq.mapsforge.theme.editor.model.*;

public class ThemeEditorPanel extends JPanel implements PropertiesPanel.PropertyChangeListener , ThemeTreeTransferHandler.OnDropSuccessListener {
    private static final Logger logger = Logger.getLogger(ThemeEditorPanel.class.getName());

    private PropertiesPanel propertiesPanel;
    private JSplitPane editorSplitPane;
    private JTree themeTree;
    private JScrollPane treeScrollPane;
    private DefaultTreeModel treeModel;
    private RenderTheme project;
    private List<ThemeChangeListener> listeners = new ArrayList<>();
    private String projectFilename;
    private String themePath;

    public ThemeEditorPanel() {
        initializeUI();
    }

    public RenderTheme getProject() {
        return this.project;
    }

    public void loadProject(String filename) {
        this.projectFilename = filename;
        // get directory from projectFilename and add theme.xml
        this.themePath = new File(new File(filename).getParent(), "theme.xml").getAbsolutePath();
        File projectFile = new File(filename);
        try {
            project = RenderThemeIO.loadProject(projectFile);
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new RuntimeException(e);
        }
        refreshTree();
    }

    public void save() {
        saveTheme();
        saveProject();
    }

    public void saveTheme() {
        File themeFile = new File(this.themePath);
        try {
            RenderThemeIO.saveTheme(project, themeFile);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error saving theme: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public void saveProject() {
        File projectFile = new File(this.projectFilename);
        try {
            RenderThemeIO.saveProject(project, projectFile);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error saving theme: " + e.getMessage(), e);
        }
    }

    public String getThemePath() {
        return this.themePath;
    }

    // Initialize UI components ========================================================================================
    private void initializeUI() {
        setLayout(new BorderLayout());

        // Create tree with sample data
        treeModel = new DefaultTreeModel(createTreeNodes());
        themeTree = new JTree(treeModel);
        themeTree.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        themeTree.setDragEnabled(true);
        themeTree.setDropMode(DropMode.ON_OR_INSERT);
        ThemeTreeTransferHandler themeTreeTransferHandler = new ThemeTreeTransferHandler();
        themeTreeTransferHandler.setOnDropSuccessListener(this);
        themeTree.setTransferHandler(themeTreeTransferHandler);
        themeTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(themeTree, BorderLayout.CENTER);
        treeScrollPane = new JScrollPane(wrapperPanel);
        treeScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        treeScrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        propertiesPanel = new PropertiesPanel();
        editorSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                treeScrollPane, propertiesPanel);
        editorSplitPane.setResizeWeight(0.75);

        add(editorSplitPane, BorderLayout.CENTER);

        JPopupMenu renderThemeContextMenu = createRenderThemeContextMenu(themeTree);
        JPopupMenu ruleContextMenu = createRuleContextMenu(themeTree);
        JPopupMenu instructionContextMenu = createInstructionContextMenu(themeTree);

        // Tree selection listener
        themeTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) themeTree.getLastSelectedPathComponent();
            if (node != null) {
                Object userObject = node.getUserObject();
                if (userObject instanceof RenderTheme || userObject instanceof Rule || userObject instanceof Instruction) {
                    propertiesPanel.updateContent(userObject);
                }
            }
        });

        themeTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    // Select the node under the mouse pointer
                    int row = themeTree.getClosestRowForLocation(e.getX(), e.getY());
                    if (row != -1) {
                        themeTree.setSelectionRow(row);
                    }
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) themeTree.getLastSelectedPathComponent();
                    if (node == null) return;
                    Object userObject = node.getUserObject();
                    JPopupMenu contextMenu;
                    if (userObject instanceof RenderTheme) {
                        contextMenu = renderThemeContextMenu;
                    } else if (userObject instanceof Rule) {
                        contextMenu = ruleContextMenu;
                    } else if (userObject instanceof Instruction) {
                        contextMenu = instructionContextMenu;
                    } else {
                        return; // No context menu for other types
                    }
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        propertiesPanel.addPropertyChangeListener(this);
    }

    private JPopupMenu createRenderThemeContextMenu(JTree tree) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("Add Rule");
        addItem.addActionListener(e -> {
            addChild(tree, new Rule());
        });
        popupMenu.add(addItem);
        return popupMenu;
    }

    private JPopupMenu createRuleContextMenu(JTree tree) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem addRule = new JMenuItem("Add Rule");
        JMenuItem addArea = new JMenuItem("Add Area");
        JMenuItem addLine = new JMenuItem("Add Line");
        JMenuItem moveToTop = new JMenuItem("Move to Top");
        JMenuItem moveUp = new JMenuItem("Move Up");
        JMenuItem moveDown = new JMenuItem("Move Down");
        JMenuItem moveToBottom = new JMenuItem("Move to Bottom");
        JMenuItem deleteItem = new JMenuItem("Delete");

        addRule.addActionListener(e -> {
            addChild(tree, new Rule());
        });
        addArea.addActionListener(e -> {
            addChild(tree, new Area());
        });
        addLine.addActionListener(e -> {
            addChild(tree, new Line());
        });
        moveUp.addActionListener(e -> {
            move(tree, -1);
        });
        moveDown.addActionListener(e -> {
            move(tree, 1);
        });
        deleteItem.addActionListener(e -> {
            deleteChild(tree);
        });

        popupMenu.add(addRule);
        popupMenu.addSeparator();
        popupMenu.add(addArea);
        popupMenu.add(addLine);
        popupMenu.addSeparator();
//        popupMenu.add(moveToTop);
        popupMenu.add(moveUp);
        popupMenu.add(moveDown);
//        popupMenu.add(moveToBottom);
        popupMenu.addSeparator();
        popupMenu.add(deleteItem);

        return popupMenu;
    }

    private JPopupMenu createInstructionContextMenu(JTree tree) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem moveUp = new JMenuItem("Move Up");
        JMenuItem moveDown = new JMenuItem("Move Down");
        JMenuItem deleteItem = new JMenuItem("Delete");
        moveUp.addActionListener(e -> {
            move(tree, -1);
        });
        moveDown.addActionListener(e -> {
            move(tree, 1);
        });
        deleteItem.addActionListener(e -> {
            deleteChild(tree);
        });
        popupMenu.add(moveUp);
        popupMenu.add(moveDown);
        popupMenu.addSeparator();
        popupMenu.add(deleteItem);
        return popupMenu;
    }

    // Move node up within parent ======================================================================================
    private void move(JTree tree, int direction) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (selectedNode == null) return;

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
        if (parentNode == null) return; // Root node or no parent

        int selectedIndex = parentNode.getIndex(selectedNode);
        if (selectedIndex <= 0 && direction == -1) return; // Already at the top or invalid index
        if (selectedIndex >= parentNode.getChildCount() - 1 && direction == 1) return; // Already at the bottom

        // Remove the selected node
        parentNode.remove(selectedNode);

        // Reinsert it at the previous position
        parentNode.insert(selectedNode, selectedIndex + direction);

        // Update the tree model
        treeModel.nodesWereRemoved(parentNode, new int[]{selectedIndex}, new Object[]{selectedNode});
        treeModel.nodesWereInserted(parentNode, new int[]{selectedIndex + direction});

        // Refresh the project structure and save
        refreshProject();
        save();
        notifyThemeChanged();

        // Reselect the moved node
        TreePath newPath = new TreePath(getPathToRoot(selectedNode));
        tree.setSelectionPath(newPath);
        tree.scrollPathToVisible(newPath);
    }

    private TreeNode[] getPathToRoot(TreeNode aNode) {
        return getPathToRoot(aNode, 0);
    }

    private TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
        TreeNode[] retNodes;

        if(aNode == null) {
            if(depth == 0)
                return null;
            else
                retNodes = new TreeNode[depth];
        } else {
            depth++;
            if(aNode.getParent() == null)
                retNodes = new TreeNode[depth];
            else
                retNodes = getPathToRoot(aNode.getParent(), depth);
            retNodes[retNodes.length - depth] = aNode;
        }
        return retNodes;
    }

    // Add child node to selected node =================================================================================
    private void addChild(JTree tree, MarshalledObject child) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) return;
        Object userObject = node.getUserObject();
        if (userObject instanceof RenderTheme theme) {
            theme.addRule((Rule) child);
        }
        else if (userObject instanceof Rule rule) {
            rule.addChild(child);
        }
        save();
        refreshTree();
        notifyThemeChanged();
    }

    // Delete selected node ============================================================================================
    private void deleteChild(JTree tree) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) return;
        Object userObject = node.getUserObject();
        if (userObject instanceof Rule rule) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
            if (parentNode != null) {
                Object parentObject = parentNode.getUserObject();
                if (parentObject instanceof RenderTheme theme) {
                    theme.removeRule(rule);
                } else if (parentObject instanceof Rule parentRule) {
                    parentRule.removeChild(rule);
                }
                save();
                refreshTree();
                notifyThemeChanged();
            }
        } else if (userObject instanceof Instruction instruction) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
            if (parentNode != null) {
                Object parentObject = parentNode.getUserObject();
                if (parentObject instanceof Rule parentRule) {
                    parentRule.removeChild(instruction);
                    save();
                    refreshTree();
                    notifyThemeChanged();
                }
            }
        }
    }





    private DefaultMutableTreeNode createTreeNodes() {
        if (project != null) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(project);
            for (Rule rule : project.getRules()) {
                createNode(root, rule);
            }
            return root;
        }
        return null;
    }

    private void createNode(DefaultMutableTreeNode parent, MarshalledObject o) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(o);
        parent.add(node);

        if (o instanceof Rule rule) {
            for (MarshalledObject child : rule.getChildren()) {
                createNode(node, child);
            }
        }
    }

    private void addRule(ActionEvent e) {
        Rule newRule = new Rule();
        project.addRule(newRule);

        save();
        refreshTree();
        notifyThemeChanged();
    }

    private void addStyle(ActionEvent e) {

    }

    private void removeSelected(ActionEvent e) {
        // Implementation for removing selected nodes
    }


    // Refresh tree while preserving expanded nodes and selection ======================================================
    private void refreshTree() {
        // capture expanded nodes and selected user object before rebuilding
        List<Object> expandedUsers = getExpandedUserObjects();
        Object selectedUser = null;
        TreePath selectedPath = themeTree.getSelectionPath();

        if (selectedPath != null) {
            DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            if (selNode != null) {
                selectedUser = selNode.getUserObject();
            }
        }

        // rebuild model
        treeModel.setRoot(createTreeNodes());
        treeModel.reload();

        // restore expanded nodes
        restoreExpanded(expandedUsers);

        // restore selection
        if (selectedUser != null) {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
            TreePath newSel = findPathByUserObject(root, selectedUser);
            if (newSel != null) {
                themeTree.setSelectionPath(newSel);
                selectedPath = newSel;
            } else {
                themeTree.clearSelection();
                selectedPath = null;
            }
        }
    }

    private List<Object> getExpandedUserObjects() {
        List<Object> list = new ArrayList<>();
        if(treeModel.getRoot() == null) return list;

        TreePath rootPath = new TreePath(treeModel.getRoot());
        Enumeration<TreePath> en = themeTree.getExpandedDescendants(rootPath);
        if (en != null) {
            while (en.hasMoreElements()) {
                TreePath p = en.nextElement();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
                list.add(node.getUserObject());
            }
        }
        return list;
    }

    private void restoreExpanded(List<Object> expandedUsers) {
        if (expandedUsers == null || expandedUsers.isEmpty()) return;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        for (Object userObj : expandedUsers) {
            TreePath path = findPathByUserObject(root, userObj);
            if (path != null) {
                themeTree.expandPath(path);
            }
        }
    }

    private TreePath findPathByUserObject(DefaultMutableTreeNode node, Object target) {
        if (node == null) return null;
        Object userObj = node.getUserObject();
        if (userObj == target || (target != null && target.equals(userObj))) {
            return new TreePath(node.getPath());
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            TreePath result = findPathByUserObject(child, target);
            if (result != null) return result;
        }
        return null;
    }


    // Theme change listeners ==========================================================================================




    @Override
    public void onPropertyChanged(Object updatedObject) {
        save();
        refreshTree();
        notifyThemeChanged();
    }

    @Override
    public void onDropSuccess() {
        refreshProject();
        save();
        notifyThemeChanged();
    }

    public void addThemeChangeListener(ThemeChangeListener listener) {
        listeners.add(listener);
    }

    public interface ThemeChangeListener {
        void onThemeChanged();
    }

    private void notifyThemeChanged() {
        for(ThemeChangeListener listener : listeners) {
            listener.onThemeChanged();
        }
    }

    // Rebuild RenderTheme based on current tree structure =============================================================
    private void refreshProject() {
        if (treeModel.getRoot() == null) return;

        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();

        // Rebuild the project from the tree structure
        rebuildProjectFromTree(rootNode);

        // Save the changes
        save();
        notifyThemeChanged();
    }

    private void rebuildProjectFromTree(DefaultMutableTreeNode rootNode) {
        if (!(rootNode.getUserObject() instanceof RenderTheme)) {
            return; // Root should be RenderTheme
        }

        RenderTheme updatedTheme = (RenderTheme) rootNode.getUserObject();

        // Clear existing rules and rebuild from tree
        updatedTheme.getRules().clear();

        // Process all direct children of root (which should be Rule nodes)
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            if (childNode.getUserObject() instanceof Rule) {
                Rule rule = rebuildRuleFromTreeNode(childNode);
                updatedTheme.addRule(rule);
            }
        }

        // Update the project reference
        this.project = updatedTheme;
    }

    private Rule rebuildRuleFromTreeNode(DefaultMutableTreeNode ruleNode) {
        Rule rule = (Rule) ruleNode.getUserObject();

        // Clear existing subrules and instructions
        rule.clearChildren();

        // Process all children of this rule node
        for (int i = 0; i < ruleNode.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) ruleNode.getChildAt(i);
            Object userObject = childNode.getUserObject();

            if (userObject instanceof Rule) {
                // This is a subrule
                Rule subrule = rebuildRuleFromTreeNode(childNode);
                rule.addChild(subrule);
            } else if (userObject instanceof Instruction) {
                // This is an instruction
                rule.addChild((MarshalledObject) userObject);
            }
        }

        return rule;
    }
}