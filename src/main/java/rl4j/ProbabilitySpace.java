package rl4j;

import org.bytedeco.leptonica.DPIX;
import org.deeplearning4j.rl4j.space.ActionSpace;

import java.util.Random;

public class ProbabilitySpace implements ActionSpace<Double> {

    protected Random rd;

    public ProbabilitySpace() {
        rd = new Random();
    }

    public int getSize() {
        return 1;
    }

    public Double randomAction() {
        return rd.nextDouble();
    }

    public void setSeed(int seed) {
        rd = new Random(seed);
    }

    public Object encode(Double a) {
        return a;
    }

    public Double noOp() {
        return 0.0;
    }
}
