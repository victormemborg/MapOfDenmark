package mod.tree;

import java.io.Serializable;

public interface Bounded extends Serializable {
    boolean intersects(double[] bounds);

    double getBound(int index);

    int getBoundsLength();
}
