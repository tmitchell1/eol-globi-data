package org.eol.globi.geo;

import java.util.List;

public interface EcoRegionFinderFactory {
    EcoregionFinder createEcoregionFinder(EcoregionType type);

    List<EcoregionFinder> createAll();
}
