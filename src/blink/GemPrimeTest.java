package blink;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GemPrimeTest {

    private Gem _S3;
    public GemPrimeTest() {
        _S3 = new Gem(new GemPackedLabelling(new int[] {1, 1, 1}));
    }

    private Gem _lastG1;
    private Gem _lastG2;
    public Gem[] getParts() {
        return new Gem[] { _lastG1, _lastG2 };
    }

    public GemPrimeStatus test(Gem gem) {

        gem = gem.copy();
        gem.cancelAllDipoles();

        // composite beacause it is a connected sum with S1xS2
        if (gem.getHandleNumber() > 1) {
            // must check any of the parts is S3
            /*
            GemExhaustiveSimplifier ESG = new GemExhaustiveSimplifier(gem, 500L);
            Gem minG = ESG.getBestAttractorFound();
            if (minG != null && _S3.equals(minG)) { // this condition is minG1 is S3
                return GemPrimeStatus.PRIME; // it is S1xS2
            }
            else*/
            return GemPrimeStatus.COMPOSITE_FROM_HANDLE;
        }
        else if (gem.getHandleNumber() == 1) {
            // must check any of the parts is S3
            GemExhaustiveSimplifier ESG = new GemExhaustiveSimplifier(gem, 500L);
            Gem minG = ESG.getBestAttractorFound();
            if (minG != null && _S3.equals(minG)) { // this condition is minG1 is S3
                return GemPrimeStatus.PRIME; // it is S1xS2
            }
            else
                return GemPrimeStatus.COMPOSITE_FROM_HANDLE;
        }


        // find all quartets
        ArrayList<Quartet> quartets = gem.findAllNonTrivialQuartets();

        if (quartets.size() == 0) {
            return GemPrimeStatus.POTENTIALLY_PRIME;
        }

        // at first: no quartets, no handles ... has a prime potential
        GemPrimeStatus status = GemPrimeStatus.POTENTIALLY_PRIME;

        // find any quartet
        for (Quartet q : quartets) {

            ArrayList<HashSet<GemVertex>> partition = gem.connectedComponentsAfterQuartetRemoval(q);

            if (partition.size() == 1) {
                // non disconnection quartet implies composite with S1xS2
                return GemPrimeStatus.COMPOSITE_FROM_NON_DISCONNECTING_QUARTET;
            }

            else if (partition.size() > 2) {
                throw new RuntimeException("Don't know what to do with this");
            }

            else if (partition.size() == 2) {
                Gem gems[] = gem.breakGemOnQuartet(q, partition.get(0), partition.get(1));
                Gem G1 = gems[0];
                _lastG1 = G1;
                Gem G2 = gems[1];
                _lastG2 = G2;

                // check if G1 or G2 is equal to S3
                HomologyGroup hgS3 = _S3.homologyGroup();
                HomologyGroup hg1 = G1.homologyGroup();
                HomologyGroup hg2 = G2.homologyGroup();
                if (!hgS3.equals(hg1) && !hgS3.equals(hg2)) {
                    return GemPrimeStatus.COMPOSITE_DISCONNECTING_QUARTET_WITH_DIFFERENT_HOMOLOGY_GROUP;
                }

                if (status == GemPrimeStatus.POTENTIALLY_PRIME) {

                    // must check any of the parts is S3
                    GemExhaustiveSimplifier ESG1 = new GemExhaustiveSimplifier(G1, 120 * 1000L);
                    Gem minG1 = ESG1.getBestAttractorFound();

                    // must check any of the parts is S3
                    GemExhaustiveSimplifier ESG2 = new GemExhaustiveSimplifier(G2, 120 * 1000L);
                    Gem minG2 = ESG2.getBestAttractorFound();

                    // if someone is S3 then this is more to potential prime
                    if (!(
                            (minG1 != null && minG1.getHandleNumber() == 0 && _S3.equals(minG1)) // this condition is minG1 is S3
                            ||
                            (minG2 != null && minG2.getHandleNumber() == 0 && _S3.equals(minG2))) // this condition is minG2 is S3
                            ) {
                        _lastG1 = minG1;
                        _lastG2 = minG2;
                        status = GemPrimeStatus.POTENTIALLY_COMPOSITE;
                    }
                }
            }

            //
        }
        return status;
    }


    public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {
        GemEntry ge = App.getRepositorio().getGemById(28);

        GemPrimeTest GPS = new GemPrimeTest();

        GemPrimeStatus status = GPS.test(ge.getGem());
        System.out.println("Prime Status = "+status);

        System.exit(0);
    }
}

