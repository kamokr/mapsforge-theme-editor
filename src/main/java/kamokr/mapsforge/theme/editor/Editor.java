package kamokr.mapsforge.theme.editor;

import org.jdom2.*;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

public class Editor extends JPanel implements EditorAttributesPanel.ChangeListener, EditorTransferHandler.OnDropSuccessListener {
    private static final Logger logger = Logger.getLogger(Editor.class.getName());

    private final Model model;
    private final EditorAttributesPanel editorAttributesPanel = new EditorAttributesPanel();

    private final JTree tree;
    private final DefaultTreeModel themeTreeModel = new DefaultTreeModel(null);

    private final List<EditorChangeListener> listeners = new ArrayList<EditorChangeListener>();

    private enum MoveDirection {
        UP(-1), DOWN(1);
        private final int value;
        MoveDirection(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        public boolean equals(int other) {
            return this.value == other;
        }
    };

    public Editor(File schemaFile) throws IOException {
        model = new Model();
        model.loadSchema(schemaFile);

        setLayout(new BorderLayout());

        tree = new JTree(themeTreeModel);

        // setup drag and drop
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        EditorTransferHandler editorTransferHandler = new EditorTransferHandler();
        editorTransferHandler.setOnDropSuccessListener(this);
        tree.setTransferHandler(editorTransferHandler);
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

        tree.setCellRenderer(
                new EditorTreeCellRenderer(new TreeNodeDescriptionProvider() {
                    @Override
                    public String getDescription(Object userObject) {
                        Model.ElementBinding element = (Model.ElementBinding) userObject;
                        String name = element.getName();

                        List<String> description = new LinkedList<>();

                        boolean editorEnabled = element.getValue("editor-enabled") == null || element.getValue("editor-enabled").equals("true");
                        String editorComment = element.getValue("editor-comment");

                        switch (name) {
                            case "rule": {
                                name = element.getValue("e");
                                description.add(element.getValue("k").replace("|", " | "));
                                description.add("=");
                                description.add(element.getValue("v").replace("|", " | "));
                                break;
                            }
                            case "layer": {
                                description.add(element.getValue("id"));
                                try {
                                    if (element.getValue("enabled").equals("true")) {
                                        description.add("<i>enabled</i>");
                                    }
                                } catch (Exception e) {
//                                    System.err.println(e.getMessage());
                                }
                                try {
                                    if (element.getValue("visible").equals("true")){
                                        description.add("<i>visible</i>");
                                    }
                                } catch (Exception e) {
//                                    System.err.println(e.getMessage());
                                }
                                break;
                            }
                            case "name": {
                                description.add(element.getValue("lang"));
                                description.add(element.getValue("value"));
                                break;
                            }
                            case "overlay": {
                                description.add(element.getValue("id"));
                                break;
                            }
                        }

                        if(editorComment == null)
                            editorComment = "";
                        else
                            editorComment += " ";
                        String pattern = editorEnabled ? "<html>{0}<b>{1}</b> {2}</html>" : "<html><strike>{0}<b>{1}</b> {2}</strike></html>";

                        return MessageFormat.format(
                                pattern,
                                editorComment,
                                name,
                                String.join(" ", description));
                    }
                }));

        editorAttributesPanel.setBorder(BorderFactory.createEmptyBorder());
        editorAttributesPanel.addChangeListener(this);

        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            Object userObject = node.getUserObject();
            if (userObject instanceof Model.ElementBinding element) {
                editorAttributesPanel.setElement(element);
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setLeftComponent(new JScrollPane(tree));
        split.setRightComponent(editorAttributesPanel);
        add(split, BorderLayout.CENTER);

        JPopupMenu renderThemeContextMenu = createRenderThemeContextMenu(tree);
        JPopupMenu ruleContextMenu = createRuleContextMenu(tree);
        JPopupMenu instructionContextMenu = createInstructionContextMenu(tree);

        tree.addMouseListener(new MouseAdapter() {
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
                    int row = tree.getClosestRowForLocation(e.getX(), e.getY());
                    if (row != -1) {
                        tree.setSelectionRow(row);
                    }
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (node == null) return;
                    Model.ElementBinding eb = ((Model.ElementBinding) node.getUserObject());
                    JPopupMenu contextMenu;
                    switch (eb.getName()) {
                        case "rendertheme":
                            contextMenu = renderThemeContextMenu;
                            break;
                        case "rule":
                            contextMenu = ruleContextMenu;
                            break;
                        case "area":
                        case "caption":
                        case "circle":
                        case "line":
                        case "lineSymbol":
                        case "pathText":
                        case "symbol":
                            contextMenu = instructionContextMenu;
                            break;
                        default:
                            return;
                    }
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    public File getThemeFile() {
        return model.getThemeXmlFile();
    }

    public File getMapFile() {
        return model.getMapFile();
    }

    private JPopupMenu createRenderThemeContextMenu(JTree tree) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("Add Rule");
        addItem.addActionListener(e -> {
            addChild(tree, "rule");
        });
        popupMenu.add(addItem);
        return popupMenu;
    }

    private JPopupMenu createRuleContextMenu(JTree tree) {
        JPopupMenu popupMenu = new JPopupMenu();

        for(Model.AttributeMeta attrMeta : model.getElementMeta("rule").choices.values()) {
            JMenuItem addItem = new JMenuItem("Add " + attrMeta.name);
            addItem.addActionListener(e -> {
                addChild(tree, attrMeta.name);
            });
            popupMenu.add(addItem);
        }

        JMenuItem moveUp = new JMenuItem("Move Up");
        JMenuItem moveDown = new JMenuItem("Move Down");
        JMenuItem deleteItem = new JMenuItem("Delete");
        moveUp.addActionListener(e -> {
            move(tree, MoveDirection.UP);
        });
        moveDown.addActionListener(e -> {
            move(tree, MoveDirection.DOWN);
        });
        deleteItem.addActionListener(e -> {
            deleteChild(tree);
        });
        popupMenu.addSeparator();
        popupMenu.add(moveUp);
        popupMenu.add(moveDown);
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
            move(tree, MoveDirection.UP);
        });
        moveDown.addActionListener(e -> {
            move(tree, MoveDirection.DOWN);
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

    /**
     * Moves a tree node within its parent
     *
     * @param tree The tree component
     * @param direction The direction to move (up or down)
     */
    private void move(JTree tree, MoveDirection direction) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (selectedNode == null) return;

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
        if (parentNode == null) return; // Root node or no parent

        int selectedIndex = parentNode.getIndex(selectedNode);
        if (selectedIndex <= 0 && direction.equals(MoveDirection.UP)) return; // Already at the top or invalid index
        if (selectedIndex >= parentNode.getChildCount() - 1 && direction.equals(MoveDirection.DOWN)) return; // Already at the bottom

        // Remove the selected node
        parentNode.remove(selectedNode);

        // Reinsert it at the previous position
        parentNode.insert(selectedNode, selectedIndex + direction.getValue());

        // Update the tree model
        themeTreeModel.nodesWereRemoved(parentNode, new int[]{selectedIndex}, new Object[]{selectedNode});
        themeTreeModel.nodesWereInserted(parentNode, new int[]{selectedIndex + direction.getValue()});

        // Refresh the project structure and save
        rebuildXmlDocument();
        save();
        notifyChanged();

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

    private void addChild(JTree tree, String complexTypeName) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) return;

        Model.ElementBinding eb = (Model.ElementBinding) node.getUserObject();
        Model.ElementBinding child = eb.addChild(complexTypeName);

        switch(complexTypeName) {
            case "caption":
                child.setValue("k", "*");
                break;
            case "cat":
                child.setValue("id", "cat_id");
                break;
            case "circle":
                child.setValue("radius", "0.0");
                break;
            case "layer":
                child.setValue("id", "layer_id");
                break;
            case "lineSymbol":
                child.setValue("src", "empty");
                break;
            case "name":
                child.setValue("lang", "XX");
                child.setValue("value", "language name");
                break;
            case "overlay":
                child.setValue("id", "overlay_id");
                break;
            case "pathText":
                child.setValue("k", "*");
                break;
            case "symbol":
                child.setValue("src", "empty");
                break;
            case "rule":
                child.setValue("e", "node");
                child.setValue("k", "*");
                child.setValue("v", "*");
                break;
        }

        rebuildTree();
        editorAttributesPanel.setElement(child);
        restoreTreeSelection();
        save();

        notifyChanged();
    }

    private void deleteChild(JTree tree) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) return;

        Model.ElementBinding eb = (Model.ElementBinding) node.getUserObject();
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();

        if (parentNode != null) {
            eb.remove();

            rebuildTree();
            save();

            notifyChanged();
        }
    }

    /**
     * Loads project files and initializes related configurations
     *
     * @param projectFile The project XML file containing main project configuration information
     * @throws IOException Thrown when an I/O error occurs during file reading or writing
     * @throws JDOMException Thrown when an error occurs during XML document parsing
     */
    public void loadProject(File projectFile) throws IOException, JDOMException {
        model.loadProject(projectFile);
        model.saveTheme();
        rebuildTree();
    }

    public void createProject(File projectFile, File mapFile) throws IOException {
        model.createProject(projectFile, mapFile);
        model.saveProject();
        model.saveTheme();
    }

    public void loadTheme(File themeFile, File mapFile) throws IOException, JDOMException {
        model.loadTheme(themeFile, mapFile);
        model.saveProject();
        model.saveTheme();
        rebuildTree();
    }

    /**
     * Saves both project XML and theme XML files
     *
     * <p>This method orchestrates the saving process by calling {@link #saveProject()}
     * and {@link #saveTheme()} methods sequentially to persist both the project data
     * and the generated theme data to their respective files.</p>
     *
     * <p>The saving process involves:
     * 1. Saving the project XML file which contains all editor-specific attributes
     * 2. Saving the theme XML file which is a processed version of the project file
     *    with editor-specific attributes removed</p>
     */
    private void save() {
        logger.info("saving");
        try {
            model.saveProject();
        } catch (IOException e) {
            logger.severe("saving project failed: " + e.getMessage());
        }
        try {
            model.saveTheme();
        } catch (Exception e) {
            logger.severe("saving theme failed: " + e.getMessage());
        }
    }

    /**
     * Rebuilds the theme XML document from the tree structure
     *
     * <p>This method traverses the theme tree model and converts tree nodes back into
     * XML elements to construct a complete XML document. If the root node is null or
     * the root node's user object is not an Element type, it clears the project XML
     * document and returns.</p>
     *
     * <p>The process involves:
     * 1. Checking if the tree model has a root node
     * 2. Validating that the root node's user object is an Element
     * 3. Rebuilding the XML element hierarchy from the tree nodes
     * 4. Creating a new XML document with the reconstructed root element</p>
     */
    private void rebuildXmlDocument() {
        model.rebuild(themeTreeModel);
    }

    private void rebuildTree() {
        List<Element> expandedElements = getExpandedElements();
        rebuildTreeFromXmlDocument();
        restoreExpandedElements(expandedElements);
        restoreTreeSelection();
    }

    private void rebuildTreeFromXmlDocument() {
        Model.ElementBinding root = model.getRoot();
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(root);
        for (Model.ElementBinding child : root.getChildren()) {
            rootNode.add(createNode(child));
        }
        themeTreeModel.setRoot(rootNode);
        themeTreeModel.reload();
    }

    private DefaultMutableTreeNode createNode(Model.ElementBinding element) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(element);
        for (Model.ElementBinding child : element.getChildren()) {
            node.add(createNode(child));
        }
        return node;
    }

    private List<Element> getExpandedElements() {
        List<Element> list = new ArrayList<>();
        if(themeTreeModel.getRoot() == null) return list;

        TreePath rootPath = new TreePath(themeTreeModel.getRoot());
        Enumeration<TreePath> en = tree.getExpandedDescendants(rootPath);
        if (en != null) {
            while (en.hasMoreElements()) {
                TreePath p = en.nextElement();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
                Model.ElementBinding eb = (Model.ElementBinding) node.getUserObject();
                list.add(eb.getElement());
            }
        }
        return list;
    }

    private void restoreExpandedElements(List<Element> expandedElements) {
        if (expandedElements == null || expandedElements.isEmpty()) return;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) themeTreeModel.getRoot();
        for (Element element : expandedElements) {
            TreePath path = findPathByElement(root, element);
            if (path != null) {
                tree.expandPath(path);
            }
        }
    }

    private TreePath findPathByElement(DefaultMutableTreeNode node, Element target) {
        if (node == null) return null;
        Model.ElementBinding eb = (Model.ElementBinding) node.getUserObject();
        if (Objects.equals(target, eb.getElement()))
            return new TreePath(node.getPath());

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            TreePath path = findPathByElement(child, target);
            if (path != null) return path;
        }
        return null;
    }

    private void restoreTreeSelection() {
        Model.ElementBinding selectedElementBinding = editorAttributesPanel.getElement();
        if (selectedElementBinding != null) {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) themeTreeModel.getRoot();
            TreePath newSel = findPathByElement(root, selectedElementBinding.getElement());
            if (newSel != null) {
                tree.setSelectionPath(newSel);
            } else {
                tree.clearSelection();
            }
        }
    }


    /**
     * Interface for providing descriptive text for tree nodes.
     *
     * This interface defines a method to get a human-readable description for a tree node's user object.
     */
    public interface TreeNodeDescriptionProvider {
        /**
         * Get descriptive text for a tree node's user object.
         *
         * @param userObject The user object of the tree node
         * @return A descriptive string, or null if no description is available
         */
        String getDescription(Object userObject);
    }

    /**
     * Custom tree cell renderer for displaying formatted tree nodes.
     *
     * This renderer uses a TreeNodeDescriptionProvider to generate formatted text for each node
     * in the tree, allowing rich display of node information instead of simple toString() output.
     */
    private static class EditorTreeCellRenderer extends DefaultTreeCellRenderer {
        // The provider used to generate node descriptions
        private final TreeNodeDescriptionProvider descriptionProvider;

        /**
         * Constructor that takes a description provider.
         *
         * @param descriptionProvider The provider to use for generating node descriptions
         */
        public EditorTreeCellRenderer(TreeNodeDescriptionProvider descriptionProvider) {
            this.descriptionProvider = descriptionProvider;
        }

        /**
         * Customizes the rendering of tree cells
         *
         * Overrides the default rendering to use the description provider for generating
         * formatted text for each node. Falls back to toString() if no description is available.
         *
         * @param tree The JTree being rendered
         * @param value The value of the node being rendered
         * @param sel Whether the node is selected
         * @param expanded Whether the node is expanded
         * @param leaf Whether the node is a leaf
         * @param row The row index of the node
         * @param hasFocus Whether the node has focus
         * @return The component used to render the node
         */
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            // Call super to set up basic rendering properties
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            // Customize rendering for DefaultMutableTreeNode instances
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();

                // Generate custom description if user object exists
                if (userObject != null) {
                    String description = descriptionProvider.getDescription(userObject);
                    if (description != null) {
                        // Use custom description
                        setText(description);
                    } else {
                        // Fallback to default toString representation
                        setText(userObject.toString());
                    }
                }
            }

            return this;
        }
    }


    // Theme change listeners ==========================================================================================
    @Override
    public void onChanged() {
        save();
        rebuildTree();
        notifyChanged();
    }

    @Override
    public void onDropSuccess() {
        rebuildXmlDocument();
        save();
        notifyChanged();
    }

    public void addChangeListener(EditorChangeListener listener) {
        listeners.add(listener);
    }

    public interface EditorChangeListener {
        void onEditorChange();
    }

    private void notifyChanged() {
        for(EditorChangeListener listener : listeners) {
            listener.onEditorChange();
        }
    }
}

