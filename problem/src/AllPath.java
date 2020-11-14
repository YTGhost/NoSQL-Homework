import java.util.ArrayList;
import java.util.List;

public class AllPath {

    public static List<String> findAllPath(TreeNode root) {
        List<String> paths = new ArrayList<>();

        if(root == null) {
            return paths;
        }

        List<String> leftPaths = findAllPath(root.left);
        List<String> rightPaths = findAllPath(root.right);

        for(String path : leftPaths) {
            paths.add(root.val + "->" + path);
        }

        for(String path : rightPaths) {
            paths.add(root.val + "->" + path);
        }

        if(paths.isEmpty()) {
            paths.add("" + root.val);
        }

        return paths;
    }

    public static void main(String[] args) {
        String str = "[1,2,3,null,5]";
        TreeNode root = TreeNode.stringToTreeNode(str);
        List<String> paths = findAllPath(root);
        for(String path : paths) {
            System.out.println(path);
        }
    }
}
