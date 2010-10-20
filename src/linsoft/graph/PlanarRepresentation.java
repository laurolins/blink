package linsoft.graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.swing.JFrame;

import linsoft.Pair;
import blink.Library;

public class PlanarRepresentation {
    private HashMap<Integer,PRVertex> _mapVertices = new HashMap<Integer,PRVertex>();
    private HashMap<Integer,PREdge> _mapEdges = new HashMap<Integer,PREdge>();
    private HashMap<Integer,PRFace> _mapFaces = new HashMap<Integer,PRFace>();
    private HashMap<Integer,PRVertex> _mapVerticesThatBecameFace = new HashMap<Integer,PRVertex>();
    private HashMap<PRVertex,PRFace> _mapVertexToFace = new HashMap<PRVertex,PRFace>();

    private PRFace _externalFace;

    public PlanarRepresentation() {
    }

    public int getNumberOfFaces() {
        return _mapFaces.size();
    }

    public int getNumberOfVertices() {
        return _mapVertices.size();
    }

    public PREdge findEdgeByObject(Object o) {
        for (PREdge e: this.getEdges()) {
            if (e.getObject() != null && o.equals(e.getObject())) {
                return e;
            }
        }
        return null;
    }

    public PRVertex findVertexByObject(Object o) {
        for (PRVertex v: this.getVertices()) {
            if (v.getObject() != null && o.equals(v.getObject())) {
                return v;
            }
        }
        for (PRVertex v: this.getVerticesThatBecameFace()) {
            if (v.getObject() != null && o.equals(v.getObject())) {
                return v;
            }
        }
        return null;
    }


    public ArrayList<PRVertex> getVerticesByDegree(int degree) {
        ArrayList<PRVertex> result = new ArrayList<PRVertex>();
        for (PRVertex v: this.getVertices())
            if (v.getDegree() == degree)
                result.add(v);
        return result;
    }

    public String getDesciption() {
        StringBuffer sb = new StringBuffer();
        // sb.append("v "+this.getVertices().size()+"\n");
        for (PREdge e: this.getEdges()) {
            sb.append("e "+e.getId()+" "+e.getV1().getId()+" "+e.getV2().getId()+"\n");
        }
        for (PRFace f: this.getFaces()) {
            sb.append("f "+f.getId());
            for (PRFaceEdge e: f.getFaceEdges()) {
                sb.append(String.format(" %s%d",e.isNegative()? "-" : "",e.getEdge().getId()));
            }
            sb.append("\n");
        }
        sb.append("x "+this.getExternalFace().getId()+"\n");
        return sb.toString();
    }

    public ArrayList<PRFace> getFaces() {
        ArrayList<PRFace> result = new ArrayList<PRFace>(_mapFaces.values());
        Collections.sort(result);
        return new ArrayList<PRFace>(_mapFaces.values());
    }

    public ArrayList<PREdge> getEdges() {
        ArrayList<PREdge> list = new ArrayList<PREdge>(_mapEdges.values());
        Collections.sort(list);
        return list;
    }

    public HashMap<Integer,ArrayList<PRVertex>> getMapDegree2Vertices() {
        HashMap<Integer,ArrayList<PRVertex>> result = new HashMap<Integer,ArrayList<PRVertex>>();
        for (int i=1;i<=4;i++) {
            result.put(i,new ArrayList<PRVertex>());
        }
        for (PRVertex v: this.getVertices()) {
            int degree = v.getDegree();
            ArrayList<PRVertex> list = result.get(degree);
            if (list == null) {
                list = new ArrayList<PRVertex>();
                result.put(degree,list);
            }
            list.add(v);
        }
        return result;
    }


    public int getNumberOfEdges() {
        return _mapEdges.size();
    }

    public int getVerticesDegreeSum() {
        int result = 0;
        for (PRVertex v: this.getVertices()) {
            result += v.getDegree();
        }
        return result;
    }

    public ArrayList<PRVertex> getOriginalVertices() {
        ArrayList<PRVertex> result = new ArrayList<PRVertex>();
        for (PRVertex v: this.getVertices()) {
            if (!v.istExtraVertex())
                result.add(v);
        }
        return result;
    }

    public ArrayList<PRVertex> getVertices() {
        ArrayList<PRVertex> result = new ArrayList<PRVertex>(_mapVertices.values());
        Collections.sort(result);
        return result;
    }

    public ArrayList<PRVertex> getVerticesThatBecameFace() {
        ArrayList<PRVertex> result = new ArrayList<PRVertex>(_mapVerticesThatBecameFace.values());
        Collections.sort(result);
        return result;
    }

    public ArrayList<PREdge> getOriginalOrDegreeAdjustmentEdges() {
        ArrayList<PREdge> result = new ArrayList<PREdge>();
        for (PREdge e: this.getEdges()) {
            if (e.getOriginalId() != -1 || e.isDegreeAdjustementEdge())
                result.add(e);
        }
        return result;
    }

    public PREdge newEdge(int id, Object o, int idVertex1, int idVertex2) {
        if (_mapEdges.get(id) != null)
            throw new RuntimeException("id already exists");
        PRVertex v1 = _mapVertices.get(idVertex1);
        PRVertex v2 = _mapVertices.get(idVertex2);
        PREdge e = new PREdge(id,o,v1,v2);
        v1.addEdge(e);
        v2.addEdge(e);
        _mapEdges.put(id,e);
        return e;
    }

    public PRFace newFace(int id, Object o) {
        if (_mapFaces.get(id) != null)
            throw new RuntimeException("id already exists");
        PRFace f = new PRFace(id,o);
        _mapFaces.put(id,f);
        return f;
    }

    public PRVertex newVertex(int id, Object o) {
        if (_mapVertices.get(id) != null)
            throw new RuntimeException("id already exists");
        PRVertex v = new PRVertex(id,o);
        _mapVertices.put(id,v);
        return v;
    }

    private PRVertex newExtraVertex() {
        int maxId = Integer.MIN_VALUE;
        for (int i: _mapVertices.keySet()) {
            if (maxId < i)
                maxId = i;
        }
        maxId++;
        PRVertex v = new PRVertex(maxId,null);
        v.setExtraVertex(true);
        _mapVertices.put(maxId,v);
        return v;
    }

    private PRVertex newDegreeAdjustmentVertex(PRVertex degreeAdjustedVertex) {
        int maxId = Integer.MIN_VALUE;
        for (int i: _mapVertices.keySet()) {
            if (maxId < i)
                maxId = i;
        }
        maxId++;
        PRVertex v = new PRVertex(maxId,null);
        v.setDegreeAdjustedVertex(degreeAdjustedVertex);
        _mapVertices.put(maxId,v);
        return v;
    }

    private PREdge newUnoriginalEdge(PRVertex v1, PRVertex v2, int originalEdgeId) {
        int maxId = Integer.MIN_VALUE;
        for (int i: _mapEdges.keySet()) {
            if (maxId < i)
                maxId = i;
        }
        maxId++;
        PREdge e = new PREdge(maxId,null,v1,v2);
        e.setOriginalId(originalEdgeId);
        v1.addEdge(e);
        v2.addEdge(e);
        _mapEdges.put(maxId,e);
        return e;
    }

    private PREdge newDegreeAdjustmentEdgeEdge(PRVertex v1, PRVertex v2, int originalEdgeId) {
        PREdge e = this.newUnoriginalEdge(v1,v2,originalEdgeId);
        e.setDegreeAdjustementEdge(true);
        return e;
    }

    public void addEdgeToFace(int faceId, int edgeId, boolean positive) {
        PRFace face = _mapFaces.get(faceId);
        PREdge e = _mapEdges.get(edgeId);
        face.add(e,positive);
        e.addFace(face);
    }

    private PRFace newUnoriginalFace(int originalFaceId) {
        int maxId = Integer.MIN_VALUE;
        for (int i: _mapFaces.keySet()) {
            if (maxId < i)
                maxId = i;
        }
        maxId++;
        PRFace f = new PRFace(maxId,null);
        f.setOriginalId(originalFaceId);
        _mapFaces.put(maxId,f);
        return f;
    }

    private PRFace newAdjustmentDegreeFace() {
        int maxId = Integer.MIN_VALUE;
        for (int i: _mapFaces.keySet()) {
            if (maxId < i)
                maxId = i;
        }
        maxId++;
        PRFace f = new PRFace(maxId,null);
        f.setIsAdjustmentDegreeFace(true);
        f.setOriginalId(-1);
        _mapFaces.put(maxId,f);
        return f;
    }

    public void setExternalFace(int id) {
        _externalFace = _mapFaces.get(id);
    }

    public PRVertex getVertex(int id) {
        return _mapVertices.get(id);
    }

    public PRFace getExternalFace() {
        return _externalFace;
    }

    public String getEdgesDescription() {
        return getListOfEdgesDescription(this.getEdges());
    }

    public static String getListOfEdgesDescription(ArrayList<PREdge> list) {
        StringBuffer sb = new StringBuffer();
        for (PREdge e: list) {
            sb.append(String.format("edge %4d vertices %4d %4d faces %4d %4d\n", e.getId(),
                                             e.getV1().getId(), e.getV2().getId(),
                                             e.getFace(0).getId(),e.getFace(1).getId()));
        }
        return sb.toString();
    }

    public String getFullDescription(boolean sideInformation) {
        return getEdgesDescription()+getOrthogonalDescription(sideInformation);
    }

    public String getOrthogonalDescription(boolean sideInformation) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        ArrayList<PRFace> faces = this.getFaces();
        Collections.sort(faces);
        for (int i=0;i<faces.size();i++) {
            if (!first)
                sb.append("\n");
            sb.append(faces.get(i).getOrthogonalDescription(sideInformation));
            first = false;
        }
        return sb.toString();
    }

    public void putAVertexInEachBend() {
        int count = 0;
        ArrayList<PRFace> faces = this.getFaces();
        for (PRFace f: faces) {
            for (int i=0;i<f.size();i++) {
                // System.out.println("i: "+i+"   fsize: "+f.size());
                if (f.getNumberOfBends(i) > 0) {
                    // the number of edges on the face increases
                    // in the right direction
                    this.putAVertexInFirstBendOfEdge(f, i);
                    count++;

                    //if (count == 11)
                    //    return;
                }
            }
        }
    }

    public void putAVertexInFirstBendOfEdge(PRFace f, int edgeIndexOnF) {
        PREdge e = f.getEdge(edgeIndexOnF);
        PRFaceEdge ef = f.getFaceEdge(edgeIndexOnF);
        int bendsOnF[] = f.getBends(edgeIndexOnF);
        if (bendsOnF.length == 0)
            throw new RuntimeException();

        // make the edges to be in the correct orientation
        if (e.isBridge()) {
            int anotherIndexOnF = f.getAnotherIndexeOfBridge(edgeIndexOnF);
            throw new RuntimeException("Case not implemented!");
        }
        else {
            PRFace g = e.getOtherFace(f);
            int edgeIndexOnG = g.getFirstIndexeOfEdge(e);

            // PRVertex v1 = e.getV1();
            PRVertex v1f = ef.getV1();
            PRVertex v2f = ef.getV2();
            PRVertex newVertex = this.newExtraVertex();

            // e go from v1f to newVertex
            ef.replaceV2(newVertex);
            newVertex.addEdge(e);

            // v2f does not incide in e anymore
            v2f.removeEdge(e);

            // make newVertex to v2f
            // this new edge will be positive in f and negatice in g
            PREdge newEdge = this.newUnoriginalEdge(newVertex,v2f,e.getOriginalId());
            boolean newEdgeSignInF = true;
            boolean newEdgeSignInG = false;

            System.out.println(String.format("\n\nPut vertex on bend - Face: %d   Edge: %d   NewEdge: %d", f.getId(),e.getId(),newEdge.getId()));

            //
            int firstBendOnF = bendsOnF[0];
            int newTransitionOnF = (firstBendOnF == 0 ? 90 : 270);
            int newTransitionOnG = (firstBendOnF == 0 ? 270 : 90);
            int bendsOnG[] = g.getBends(edgeIndexOnG);

            // insert faceEdge in face f
            f.setBends(edgeIndexOnF,new int[0]);
            f.insertNewEdge(newEdge,newEdgeSignInF,edgeIndexOnF+1);
            f.setTransition(edgeIndexOnF+1,f.getTransition(edgeIndexOnF));
            f.setTransition(edgeIndexOnF,newTransitionOnF);
            f.setBends(edgeIndexOnF+1,linsoft.Library.subarray(bendsOnF,1,bendsOnF.length-1));

            // insert faceEdge in face g
            g.setBends(edgeIndexOnG,new int[0]);
            g.insertNewEdge(newEdge,newEdgeSignInG,edgeIndexOnG);
            g.setTransition(edgeIndexOnG,newTransitionOnG);
            g.setBends(edgeIndexOnG,linsoft.Library.subarray(bendsOnG,0,bendsOnG.length-1));

            // add f and g faces to newEdge
            newEdge.addFace(f);
            newEdge.addFace(g);
        }
    }

    public PREdge subdivideEdge(PRFace f, int edgeIndexOnF) {
        PREdge e = f.getEdge(edgeIndexOnF);

        System.out.println(String.format("\n\n SUBDIVIDE EDGE %d following face %d\n\n",e.getId(),f.getId()));

        PRFaceEdge ef = f.getFaceEdge(edgeIndexOnF);
        if (e.isBridge()) {
            throw new RuntimeException("Case not implemented!");
        }
        else {
            PRFace g = e.getOtherFace(f);
            int edgeIndexOnG = g.getFirstIndexeOfEdge(e);

            // PRVertex v1 = e.getV1();
            PRVertex v1f = ef.getV1();
            PRVertex v2f = ef.getV2();
            PRVertex newVertex = this.newExtraVertex();

            // e go from v1f to newVertex
            ef.replaceV2(newVertex);
            newVertex.addEdge(e);

            // v2f does not incide in e anymore
            v2f.removeEdge(e);

            // make newVertex to v2f
            // this new edge will be positive in f and negatice in g
            PREdge newEdge = this.newUnoriginalEdge(newVertex,v2f,e.getOriginalId());
            boolean newEdgeSignInF = true;
            boolean newEdgeSignInG = false;

            // insert faceEdge in face f
            f.insertNewEdge(newEdge,newEdgeSignInF,edgeIndexOnF+1);
            f.setTransition(edgeIndexOnF+1,f.getTransition(edgeIndexOnF));
            f.setTransition(edgeIndexOnF,180);

            // insert faceEdge in face g
            g.insertNewEdge(newEdge,newEdgeSignInG,edgeIndexOnG);
            g.setTransition(edgeIndexOnG,180);


            // add f and g faces to newEdge
            newEdge.addFace(f);
            newEdge.addFace(g);

            // return new edge
            return newEdge;
        }
    }

    /**
     * Returns the set of edges divided into two sets, each
     * with edges with the same angle (horizontal or vertical)
     */
    public void mounSideInformationOnPlanarRepresentation() {
        HashSet<PRFace> UF = new HashSet<PRFace>(this.getFaces()); // unprocessde faces
        HashSet<PRFace> PF = new HashSet<PRFace>(); // processed faces
        HashSet<PRFace> SF = new HashSet<PRFace>(); // face on the stack

        int side = 0;
        int index = 0;

        PRFace f = this.getExternalFace();

        Stack<Pair> S = new Stack<Pair>(); // stack edges
        while (true) {

            UF.remove(f); // f not unprocessed anymore

            // go in all edges of this face
            for (int i=0;i<f.size();i++) {
                PRFaceEdge ef = f.getFaceEdge((index + i) % f.size());
                PREdge e = ef.getEdge();

                //System.out.println(String.format("Trying to tag edge %d with flag %d",e.getId(),flag));
                //if (result[(flag + 1)%2].contains(e))
                //    throw new RuntimeException("OOOOpppsss");
                ef.setSide(side);

                if (ef.getVertexTransition() == 90) {
                    side = (side + 1) % 4;
                }
                else if (ef.getVertexTransition() == 180) {
                    side = side;
                }
                else if (ef.getVertexTransition() == 270) {
                    side = (4 + (side - 1)) % 4;
                }

                // push another candidate into the stack? (note that no
                // bridges enter the stack)
                if (!e.isBridge()) {
                    PRFace otherFace = e.getOtherFace(f);
                    if (UF.contains(otherFace) && !SF.contains(otherFace)) {
                        // System.out.println(String.format("Push edge %d and face %d into the stack", e.getId(), otherFace.getId()));
                        S.push(new Pair(ef, otherFace));
                        SF.add(otherFace);
                    }
                }
            }

            PF.add(f); // f is processed

            boolean finished = true;
            while (!S.isEmpty()) {
                Pair pair = S.pop();
                f = (PRFace) pair.getSecond();
                SF.remove(f);
                if (!UF.contains(f))
                    continue;
                PRFaceEdge ef = (PRFaceEdge) pair.getFirst();
                side = (ef.getSide()+2) % 4;
                index = f.indexOf(ef.getEdge()); // unique once ee is not a bridge
                finished = false;
                // System.out.println(String.format("Pop edge %d on face %d flag %d",ef.getEdge().getId(),f.getId(),side));
                break;
            }

            if (finished)
                break;
        }

    }

    public PRFace subdivideFace(
            PRFace f,
            PRVertex v1,
            PRVertex v2,
            int closeEdgeFirstAngleInF,
            int closeEdgeSecondAngleInF,
            int transitionToCloseEdgeOnOtherFace,
            int closeEdgeTransitionOnOtherFace) {

        System.out.println(String.format("\n\n SUBDIVIDE FACE %d on vertices %d and %d\n\n",f.getId(),v1.getId(),v2.getId()));

        // find indexes to divide face
        int i1 = -1, i2 = -1;
        int n = f.size();
        for (int i=0;i<n;i++) {
            if (f.getFaceEdge(i).getV2() == v1)
                i1 = i;
            if (f.getFaceEdge(i).getV2() == v2)
                i2 = i;
        }
        if (i1 == -1 || i2 == -1)
            throw new RuntimeException("OOOOOppppssss");

        // System.out.println(String.format("e[i1] = %d    e[i2] = %d",));

        // create new edge. it will be positive in
        // F and negative in the new face
        PREdge closeEdge = this.newUnoriginalEdge(v1,v2,-1);
        boolean closeEdgeSignInF = true;
        boolean closeEdgeSignInNewFace = false;

        // copy edges of f at indexes i0+1 to i2
        PRFace newFace = this.newUnoriginalFace(f.getId());
        newFace.add(closeEdge,closeEdgeSignInNewFace,closeEdgeTransitionOnOtherFace);

        // copy them to new face with the same orientation
        ArrayList<PRFaceEdge> moveEdges = f.getFaceEdges((i1 + 1)%n,i2);
        for (int i=0;i<moveEdges.size();i++) {
            PRFaceEdge ef = moveEdges.get(i);
            // their default values orthogonal representation are correct
            // on the new face
            if (i < moveEdges.size() - 1)
                newFace.add(ef.getEdge(), ef.isPositive(), ef.getVertexTransition());
            else if (i == moveEdges.size() - 1)
                newFace.add(ef.getEdge(), ef.isPositive(), transitionToCloseEdgeOnOtherFace);
            else throw new RuntimeException();
            ef.getEdge().addFace(newFace);

            // use this loop also to remove face f from these edges
            ef.getEdge().removeFace(f);
        }

        // remove edges from f
        f.setTransition(i1,closeEdgeFirstAngleInF);
        f.insertNewEdge(closeEdge,closeEdgeSignInF,i1+1);
        f.setTransition(i1+1,closeEdgeSecondAngleInF);
        f.removeFaceEdges(moveEdges);

        // add to face
        closeEdge.addFace(f);
        closeEdge.addFace(newFace);

        //
        return newFace;
    }

    public void divideExternalFaceInRectangles(PRFace f) {
        while (createExternalRectangleIfThereIsOne(f) || createRectangleIfNecessary(f)) {
            // System.out.println("\n\nFACE DIVISION RESULT\n"+this.getFullDescription(false));
            continue;
        }
        // makeOneExternalFaceDivisionIfNecessary(f);
    }

    public void divideInternalFaceInRectangles(PRFace f) {
        while (createRectangleIfNecessary(f)) {
            // System.out.println("\n\nFACE DIVISION RESULT\n"+this.getFullDescription(false));
            // System.exit(0);
            continue;
        }
        //makeOneInternalFaceDivisionIfNecessary(f);
    }

    public boolean createRectangleIfNecessary(PRFace f) {
        ArrayList<Integer> specialIndexes = new ArrayList<Integer>();
        for (int i=0;i<f.size();i++) {
            if (f.getTransition(i) != 180) {
                specialIndexes.add(i);
            }
        }
        int n = specialIndexes.size();
        if (n<=4)
            return false;

        int j=0;
        while (j < n) {

            int i0 = specialIndexes.get((n + j - 3) % n);
            int i1 = specialIndexes.get((n + j - 2) % n);
            int i2 = specialIndexes.get((n + j - 1) % n);
            int i3 = specialIndexes.get((n + j - 0) % n);
            int i4 = specialIndexes.get((n + j + 1) % n);
            int i5 = specialIndexes.get((n + j + 2) % n);
            int vi[] = {i0,i1,i2,i3,i4,i5};

            int t0 = f.getTransition(i0);
            int t1 = f.getTransition(i1);
            int t2 = f.getTransition(i2);
            int t3 = f.getTransition(i3);
            int t4 = f.getTransition(i4);
            int t5 = f.getTransition(i5);
            int vt[] = {t0,t1,t2,t3,t4,t5};

            if ((t0 == 270 || t0 == 360) && t1 == 90 && t2 == 90) {

                System.out.print("Found a face subdivision point.");

                if (t3 == 90) { // needs new vertex?

                    System.out.println("Surgery of type: 1000 with new vertex creation");

                    int indexOfEdgeToSubdivide = (i2 + 1) % f.size();

                    System.out.println(String.format("Face: %d  ei0: %d  ei1: %d  ei2: %d  ei3: %d",
                                       f.getId(),
                                       f.getEdge(i0).getId(),
                                       f.getEdge(i1).getId(),
                                       f.getEdge(i2).getId(),
                                       f.getEdge(i3).getId()));

                    // check if it needs a new vertex.
                    if (f.getTransition(indexOfEdgeToSubdivide) == 180) { // it doesn't need!

                        System.out.println("Doesn't need vertex creation");

                        //
                        PRFaceEdge fe0 = f.getFaceEdge(i0);
                        int closeEdgeFirstAngleOnF = fe0.getVertexTransition()==270 ? 180 : 270;

                        // the first edge after the 100 transition
                        PRVertex v1CloseEdge = f.getFaceEdge(i0).getV2();
                        PRVertex v2CloseEdge = f.getFaceEdge(indexOfEdgeToSubdivide).getV2();
                        // System.out.println(String.format("closeEdge v1 of %d is %d",f.getEdge(i0).getId(),v1CloseEdge.getId()));

                        // divide edgeToSubdivide (subdivies in the orientation of f)
                        // subEdge goes from newVertex to old fe.getV2()
                        // this may change the indexes i0, i1, i2, i3
                        System.out.println(String.format("closeEdge is on vertices %d %d",v1CloseEdge.getId(),v2CloseEdge.getId()));

                        // subdivide the face
                        this.subdivideFace(f,v1CloseEdge,v2CloseEdge,closeEdgeFirstAngleOnF,90,90,90);

                        // finished surgery
                        return true;
                    }

                    else { // it needs

                        System.out.println("Needs vertex creation");

                        //
                        int closeEdgeFirstAngleOnF = f.getFaceEdge(i0).getVertexTransition()==270 ? 180 : 270;

                        // the first edge after the 100 transition
                        PRVertex v1CloseEdge = f.getFaceEdge(i0).getV2();
                        // System.out.println(String.format("closeEdge v1 of %d is %d",f.getEdge(i0).getId(),v1CloseEdge.getId()));

                        // divide edgeToSubdivide (subdivies in the orientation of f)
                        // subEdge goes from newVertex to old fe.getV2()
                        // this may change the indexes i0, i1, i2, i3
                        PREdge subEdge = this.subdivideEdge(f,indexOfEdgeToSubdivide);
                        PRVertex v2CloseEdge = subEdge.getV1();

                        System.out.println(String.format("closeEdge is on vertices %d %d",v1CloseEdge.getId(),v2CloseEdge.getId()));

                        // subdivide the face
                        this.subdivideFace(f,v1CloseEdge,v2CloseEdge,closeEdgeFirstAngleOnF,90,90,90);

                        // finished surgery
                        return true;
                    }
                }
            }

            int[][] patternAndOutput = {
                              {270,90,90,270},{180,180,90,90},
                              {360,90,90,270},{270,180,90,90},
                              {270,90,90,360},{180,270,90,90},
                              {360,90,90,360},{270,270,90,90},
            };

            for (int i = 0; i < patternAndOutput.length; i += 2) {
                // try to match
                boolean match = true;
                if (n >= patternAndOutput[i].length) {
                    for (int k = 0; k < patternAndOutput[i].length; k++) {
                        if (patternAndOutput[i][k] != vt[k]) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        System.out.println(String.format(
                                "SUBDIVIDE INTERNAL FACE (case %s): Face: %d  ei0: %d  ei1: %d  ei2: %d  ei3: %d  ei4: %d  ei5: %d",
                                Library.intArrayToString(patternAndOutput[i]),
                                f.getId(),
                                f.getEdge(i0).getId(),
                                f.getEdge(i1).getId(),
                                f.getEdge(i2).getId(),
                                f.getEdge(i3).getId(),
                                f.getEdge(i4).getId(),
                                f.getEdge(i5).getId()));
                        PRVertex v1f = f.getFaceEdge(vi[0]).getV2();
                        PRVertex v2f = f.getFaceEdge(vi[patternAndOutput[i].length - 1]).getV2();
                        this.subdivideFace(f, v1f, v2f,
                                           patternAndOutput[i + 1][0],
                                           patternAndOutput[i + 1][1],
                                           patternAndOutput[i + 1][2],
                                           patternAndOutput[i + 1][3]);
                        return true;
                    }

                }
            }
            j++;
        }
        return false;
    }


    /**
     * Returns true if it was necessary. False otherwise.
     */
    public boolean createExternalRectangleIfThereIsOne(PRFace f) {

        ArrayList<Integer> specialIndexes = new ArrayList<Integer>();
        for (int i=0;i<f.size();i++) {
            if (f.getTransition(i) != 180) {
                specialIndexes.add(i);
            }
        }

        int n = specialIndexes.size();
        if (n<4)
            return false;

        int j=0;
        while (j < n) {
            int i0 = specialIndexes.get((n + j - 3) % n);
            int i1 = specialIndexes.get((n + j - 2) % n);
            int i2 = specialIndexes.get((n + j - 1) % n);
            int i3 = specialIndexes.get((n + j - 0) % n);
            int i4 = specialIndexes.get((n + j + 1) % n);
            int i5 = specialIndexes.get((n + j + 2) % n);
            int vi[] = {i0,i1,i2,i3,i4,i5};

            int t0 = f.getTransition(i0);
            int t1 = f.getTransition(i1);
            int t2 = f.getTransition(i2);
            int t3 = f.getTransition(i3);
            int t4 = f.getTransition(i4);
            int t5 = f.getTransition(i5);
            int vt[] = {t0,t1,t2,t3,t4,t5};


            // CASE 101
            if ((t0 == 270 || t0 == 360) && t1 == 90 && (t2 == 270 || t2 == 360)) {

                System.out.println(String.format("SUBDIVIDE EXTERNAL FACE (case 101): Face: %d  ei0: %d  ei1: %d  ei2: %d",
                                   f.getId(),
                                   f.getEdge(i0).getId(),
                                   f.getEdge(i1).getId(),
                                   f.getEdge(i2).getId()));


                // create 1 new face and 2 new edges one from
                //
                PRVertex v1f = f.getFaceEdge(i0).getV2();
                PRVertex v2f = f.getFaceEdge(i2).getV2();
                PRVertex nv = this.newExtraVertex();

                int closeEdgeFirstAngleOnF = f.getFaceEdge(i0).getVertexTransition()==270 ? 180 : 270;
                int closeEdgeLastAngleOnF = f.getFaceEdge(i2).getVertexTransition()==270 ? 180 : 270;

                PREdge ne1 = this.newUnoriginalEdge(v1f,nv,-1);
                PREdge ne2 = this.newUnoriginalEdge(nv,v2f,-1);

                PRFace newFace = this.newUnoriginalFace(f.getId());
                newFace.add(ne2,false);
                newFace.add(ne1,false);

                // copy them to new face with the same orientation


                ArrayList<PRFaceEdge> moveEdges = f.getFaceEdges((i0+1)%f.size(),i2);
                for (int i=0;i<moveEdges.size();i++) {
                    PRFaceEdge ef = moveEdges.get(i);
                    // their default values orthogonal representation are correct
                    // on the new face
                    if (i < moveEdges.size() - 1)
                        newFace.add(ef.getEdge(), ef.isPositive(), ef.getVertexTransition());
                    else
                        newFace.add(ef.getEdge(), ef.isPositive(), 90);
                    ef.getEdge().addFace(newFace);

                    // use this loop also to remove face f from these edges
                    ef.getEdge().removeFace(f);
                }

                // remove edges from f
                f.setTransition(i0,closeEdgeFirstAngleOnF);
                f.insertNewEdge(ne1,true,i0+1);
                f.insertNewEdge(ne2,true,i0+2);
                f.setTransition(i0+1,270);
                f.setTransition(i0+2,closeEdgeLastAngleOnF);
                f.removeFaceEdges(moveEdges);

                // add to face
                ne1.addFace(f);
                ne1.addFace(newFace);
                ne2.addFace(f);
                ne2.addFace(newFace);

                return true;
            }
            j++;
        }
        return false;
    }

    public double[] getPathOfEdge(int edgeId) {

        // do a search on the edges
        PREdge startingEdge = null;
        for (PREdge e: this.getEdges()) {
            if (e.getOriginalId() == edgeId) {
                startingEdge = e;
                break;
            }
        }
        if (startingEdge == null)
            throw new RuntimeException();

        // linked list
        LinkedList<PRVertex> path = new LinkedList<PRVertex>();
        PRVertex h1 = startingEdge.getV1();
        PRVertex h2 = startingEdge.getV2();
        path.add(h1);
        path.add(h2);

        HashSet<PREdge> processedEdges = new HashSet<PREdge>();
        processedEdges.add(startingEdge);

        Stack<PRVertex> S = new Stack<PRVertex>();
        S.push(h1);
        S.push(h2);

        while (!S.isEmpty()) {
            PRVertex v = S.pop();
            for (PREdge e: v.getEdges()) {
                if (processedEdges.contains(e))
                    continue;
                if (e.getOriginalId() != edgeId)
                    continue;
                processedEdges.add(e);
                PRVertex opposite = e.getOpposite(v);
                if (v == h1) {
                    path.add(0,opposite);
                    h1 = opposite;
                    if (!S.contains(opposite))
                        S.push(opposite);
                    break; // a loop and we are finished
                }
                else if (v == h2) {
                    path.add(opposite);
                    h2 = opposite;
                    if (!S.contains(opposite))
                        S.push(opposite);
                    break; // a loop and we are finished
                }
            }
        }

        // boolean is a loop?
        boolean isLoop = path.get(0) == path.get(path.size()-1);

        if (isLoop)
            path.remove(path.size()-1);

        // find first non extravertes
        int index = -1;
        for (int i=0;i<path.size();i++) {
            if (!path.get(i).istExtraVertex()) {
                index = i;
                break;
            }
        }
        Collections.rotate(path,-index);

        if (path.get(0).isDegreeAdjustedVertex())
            path.add(0,path.get(0).getDegreeAdjustedVertex());
        if (path.get(path.size()-1).isDegreeAdjustedVertex())
            path.add(path.get(path.size()-1).getDegreeAdjustedVertex());


        if (isLoop)
            path.add(path.get(0));


        double result[] = new double[path.size() * 2];
        int i=0;
        for (PRVertex v: path) {
            result[i++] = v.getX();
            result[i++] = v.getY();
        }

        return result;

    }

    /**
     * Returns the set of edges divided into two sets, each
     * with edges with the same angle (horizontal or vertical)
     */
    public void positionEachVertex() {
        HashSet<PRFace> UF = new HashSet<PRFace>(this.getFaces()); // unprocessde faces
        HashSet<PRFace> PF = new HashSet<PRFace>(); // processed faces
        HashSet<PRFace> SF = new HashSet<PRFace>(); // face on the stack

        PRFace f = this.getExternalFace();

        double minX = 0;
        double minY = 0;

        int index = 0;
        f.getFaceEdge(index).getV1().setPosition(0,0);
        double currentPos[] = {0, 0};

        Stack<Pair> S = new Stack<Pair>(); // stack edges
        while (true) {

            UF.remove(f); // f not unprocessed anymore

            // go in all edges of this face
            for (int i=0;i<f.size();i++) {

                PRFaceEdge ef = f.getFaceEdge((index + i) % f.size());
                PREdge e = ef.getEdge();

                int dx = 0,dy = 0;
                if (ef.getSide() == 0) { dx = -1; dy = 0; }
                else if (ef.getSide() == 1) { dx = 0; dy = 1; }
                else if (ef.getSide() == 2) { dx = 1; dy = 0; }
                else if (ef.getSide() == 3) { dx = 0; dy = -1; }
                else throw new RuntimeException();

                PRVertex v2 = ef.getV2();
                int length = e.getLength();
                double xx = currentPos[0]+dx*length;
                double yy = currentPos[1]+dy*length;
                v2.setPosition(xx,yy);

                // position vertex
                // System.out.println(String.format("Position vertex %d at %d %d",v2.getId(),xx,yy));

                currentPos[0] = xx;
                currentPos[1] = yy;

                if (minX > xx) minX = xx;
                if (minY > yy) minY = yy;

                // push another candidate into the stack? (note that no
                // bridges enter the stack)
                if (!e.isBridge()) {
                    PRFace otherFace = e.getOtherFace(f);
                    if (UF.contains(otherFace) && !SF.contains(otherFace)) {
                        // System.out.println(String.format("Push edge %d and face %d into the stack",e.getId(),otherFace.getId()));
                        S.push(new Pair(ef, otherFace));
                        SF.add(otherFace);
                    }
                }
            }

            PF.add(f); // f is processed

            boolean finished = true;
            while (!S.isEmpty()) {
                Pair pair = S.pop();
                f = (PRFace) pair.getSecond();
                SF.remove(f);
                if (!UF.contains(f))
                    continue;
                PRFaceEdge ef = (PRFaceEdge) pair.getFirst();
                index = f.indexOf(ef.getEdge()); // unique once ee is not a bridge
                ef = f.getFaceEdge(index);
                currentPos[0] = ef.getV1().getX();
                currentPos[1] = ef.getV1().getY();
                finished = false;
                //System.out.println(String.format("Pop edge %d on face %d index %d",ef.getEdge().getId(),f.getId(),index));
                break;
            }

            if (finished)
                break;
        }

        // translate to origin
        for (PRVertex v: this.getVertices()) {
            v.setPosition(v.getX()-minX,v.getY()-minY);
        }


        // assign position to adjusted vertices
        for (PRVertex v: this.getVerticesThatBecameFace()) {
            double mminX = Integer.MAX_VALUE;
            double mminY = Integer.MAX_VALUE;
            double mmaxX = Integer.MIN_VALUE;
            double mmaxY = Integer.MIN_VALUE;
            for (PRVertex vv: this.getVertices()) {
                if (vv.getDegreeAdjustedVertex() == v) {
                    if (vv.getX() < mminX ) mminX = vv.getX();
                    if (vv.getY() < mminY ) mminY = vv.getY();
                    if (vv.getX() > mmaxX ) mmaxX = vv.getX();
                    if (vv.getY() > mmaxY ) mmaxY = vv.getY();
                }
            }
            v.setPosition((mmaxX+mminX)/2.0,(mmaxY+mminY)/2.0);
        }


        for (PRVertex v: this.getVertices()) {
            // System.out.println(String.format("Vertex %d position %d %d",v.getId(),v.getX(),v.getY()));
        }
    }

    public void createFacesOnVerticesWithDegreeGreaterThan4() {
        ArrayList<PRVertex> vertices = this.getVertices();
        for (PRVertex v: vertices) {
            if (v.getDegree() <= 4)
                continue;

            ArrayList<PRTransition> transitions = v.getTransitions();

            int n = transitions.size();
            if (n != v.getDegree())
                throw new RuntimeException("ooops");
            PRVertex vs[] = new PRVertex[n];
            PREdge es[] = new PREdge[n];
            for (int i=0;i<n;i++) {
                vs[i] = this.newDegreeAdjustmentVertex(v);
            }
            { // set corners
                int q = n / 4;
                int r = n % 4;
                int corners[] = null;
                if (r == 0)
                    corners = new int[] {0, q, 2 * q, 3 * q};
                else if (r == 1)
                    corners = new int[] {0, q + 1, 2 * q + 1, 3 * q + 1};
                else if (r == 2)
                    corners = new int[] {0, q + 1, 2 * q + 2, 3 * q + 2};
                else //if (r == 3)
                    corners = new int[] {0, q + 1, 2 * q + 2, 3 * q + 3};
                vs[corners[0]].setIsCornerDegreeAdjustmentVertex(true);
                vs[corners[1]].setIsCornerDegreeAdjustmentVertex(true);
                vs[corners[2]].setIsCornerDegreeAdjustmentVertex(true);
                vs[corners[3]].setIsCornerDegreeAdjustmentVertex(true);
            }


            for (int i=0;i<n;i++) {
                es[i] = this.newDegreeAdjustmentEdgeEdge(vs[i],vs[(i+1)%n],-1);
            }

            PRFace newFace = this.newAdjustmentDegreeFace();
            v.removeAllEdges();
            _mapVertices.remove(v.getId());
            _mapVerticesThatBecameFace.put(v.getId(),v);
            _mapVertexToFace.put(v,newFace);

            // in reverse order
            for (int i=n-1;i>=0;i--) {
                newFace.add(es[i],false);
                es[i].addFace(newFace);
            }

            for (int i=0;i<n;i++) {
                PRTransition ti = transitions.get(i);
                PRFaceEdge fe1 = ti.getFaceEdge1();
                //PRFaceEdge fe2 = ti.getFaceEdge2();
                fe1.replaceV2(vs[i]);
                vs[i].addEdge(fe1.getEdge());
                //if (i < n - 1)
                //    fe2.replaceV1(vs[i]);
                PRFace f = ti.getFace();
                int index = f.indexOf(ti.getFaceEdge2());
                f.insertNewEdge(es[i],true,index);
                es[i].addFace(f);
            }

            /*
            // found a vertex that will be transormed into a face
            int vBySide = (v.getDegree()-4) / 4;
            int vRemain = (v.getDegree()-4) % 4;
            int nSide[] = {vBySide,vBySide,vBySide,vBySide};
            for (int i=0;i<vRemain;i++)
                nSide[i]++;

            PRVertex corners[] = {
                this.newExtraVertex(),
                this.newExtraVertex(),
                this.newExtraVertex(),
                this.newExtraVertex()};
            PRVertex side01[] = new PRVertex[nSide[0]];
            PRVertex side12[] = new PRVertex[nSide[1]];
            PRVertex side23[] = new PRVertex[nSide[2]];
            PRVertex side30[] = new PRVertex[nSide[3]];
            */
        }
    }


    public static void main(String[] args) throws Exception {

        PlanarRepresentation P = PlanarRepresentation.loadFromFile("res/ex9.pr");
        StringBuffer sb = new StringBuffer();
        for (PRVertex v: P.getVertices()) {
            ArrayList<PRTransition> ts = v.getTransitions();
            sb.append(String.format("v%d",v.getId()));
            for (PRTransition t: ts) {
                sb.append(String.format(" %s",t.toString()));
            }
            sb.append("\n");
        }
        System.out.println(""+sb.toString());


        //
        System.out.println("\n\nBEFORE\n\n"+P.getDesciption());

        System.out.println("\n\nAFTER\n\n");

        P.createFacesOnVerticesWithDegreeGreaterThan4();

        System.out.println(""+P.getDesciption());

        new OrthogonalLayout(P);

        JFrame f = new JFrame("Tamassia's Algorithm");
        linsoft.gui.util.Library.resizeAndCenterWindow(f, 640, 480);
        f.setContentPane(new PanelPlanarRepresentation(P));
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setVisible(true);

    }



    /**
     * loadFromFile
     */
    public static PlanarRepresentation loadFromFile(String fileName) throws Exception {

        BufferedReader fr = new BufferedReader(new FileReader(fileName));
        String s;

        PlanarRepresentation P = new PlanarRepresentation();

        while ( (s = fr.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(s," ,\t");
            String code = st.nextToken().trim();
            if ("e".equals(code)) {
                int id = Integer.parseInt(st.nextToken().trim());
                int v1 = Integer.parseInt(st.nextToken().trim());
                int v2 = Integer.parseInt(st.nextToken().trim());
                if (P.getVertex(v1) == null)
                    P.newVertex(v1,null);
                if (P.getVertex(v2) == null)
                    P.newVertex(v2,null);
                P.newEdge(id,null,v1,v2);
            }
            else if ("f".equals(code)) {
                int id = Integer.parseInt(st.nextToken().trim());
                PRFace f = P.newFace(id,null);
                while (st.hasMoreTokens()) {
                    int e = Integer.parseInt(st.nextToken().trim());
                    boolean positive = true;
                    if (e < 0) {
                        positive = false;
                        e = -e;
                    }
                    P.addEdgeToFace(id,e,positive);
                }
            }
            else if ("x".equals(code)) {
                int id = Integer.parseInt(st.nextToken().trim());
                P.setExternalFace(id);
            }
        }
        return P;
    }


    public double[] getBounds() {
        double minX = +1e+20;
        double minY = +1e+20;
        double maxX = -1e+20;
        double maxY = -1e+20;
        for (PRVertex v: this.getVertices()) {
            if (minX > v.getX()) minX = v.getX();
            if (minY > v.getY()) minY = v.getY();
            if (maxX < v.getX()) maxX = v.getX();
            if (maxY < v.getY()) maxY = v.getY();
        }
        for (PRVertex v: this.getVerticesThatBecameFace()) {
            if (minX > v.getX()) minX = v.getX();
            if (minY > v.getY()) minY = v.getY();
            if (maxX < v.getX()) maxX = v.getX();
            if (maxY < v.getY()) maxY = v.getY();
        }
        return new double[]{minX,minY,maxX,maxY};
    }

    public boolean testTransitionsOnExternalFaceAfterRectangles() {
        int count270 = 0;
        for (PRFaceEdge e: _externalFace.getFaceEdges()) {
            if (e.getVertexTransition() == 270) {
                count270++;
                if (count270 > 4)
                    return false;
            }
            else if (e.getVertexTransition() != 180) {
                return false;
            }
        }
        return true;
    }

}







