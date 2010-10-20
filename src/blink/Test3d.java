package blink;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class Test3d extends JFrame {
    private Transform3D rotate1 = new Transform3D();
    private Transform3D rotate2 = new Transform3D();

   public Test3d() {
     super("Test3d");
     Canvas3D canvas3D = createCanvas3D();
     BranchGroup scene = createSceneGraph();
     connect(canvas3D, scene);
   }

   private Canvas3D createCanvas3D() {
     setSize(300, 300);
     getContentPane().setLayout(new BorderLayout());
     GraphicsConfiguration config =
       SimpleUniverse.getPreferredConfiguration();
     Canvas3D canvas3D = new Canvas3D(config);
     setSize(300, 300);
     getContentPane().add(canvas3D);
     return canvas3D;
   }

   private BranchGroup createSceneGraph() {
     BranchGroup objRoot = new BranchGroup();
     TransformGroup rotator = new TransformGroup(rotateCube());

     Appearance a = new Appearance();
     a.setColoringAttributes(new ColoringAttributes(0.3f,1.0f,0.2f,ColoringAttributes.NICEST));

     objRoot.addChild(rotator);
     rotator.addChild(new ColorCube(0.3));

     Transform3D t = new Transform3D();
     t.setTranslation(new Vector3d(0.2,0.2,0.2));
     TransformGroup translator = new TransformGroup(t);
     objRoot.addChild(translator);
     translator.addChild(new Sphere(0.1f,a));

     objRoot.compile();
     return objRoot;
   }

   private Transform3D rotateCube() {
     rotate1.rotX(Math.PI / 6.0d);
     rotate2.rotY(Math.PI / 4.0d);
     rotate1.mul(rotate2);
     return rotate1;
   }

   private void connect(Canvas3D canvas3D,
                                BranchGroup scene) {
     SimpleUniverse simpleU =
                       new SimpleUniverse(canvas3D);
     simpleU.getViewingPlatform().
                       setNominalViewingTransform();
     simpleU.addBranchGraph(scene);
   }

   public static void main(String[] args) {
     new Test3d().setVisible(true);
   }
}
