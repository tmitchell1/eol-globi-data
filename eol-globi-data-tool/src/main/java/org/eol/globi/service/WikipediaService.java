package org.eol.globi.service;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static org.eol.globi.domain.TaxonomyProvider.WIKIPEDIA;

public class WikipediaService extends BaseTaxonIdService {

    public static final String PHYTOPLANKTON = WIKIPEDIA + "Phytoplankton";
    public static final String PLANKTON = WIKIPEDIA + "Plankton";
    public static final String ZOOPLANKTON = WIKIPEDIA + "Zooplankton";
    public static final String INVERTEBRATA = WIKIPEDIA + "Invertebrata";
    public static final String BACTERIOPLANKTON = WIKIPEDIA + "Bacterioplankton";

    private Map<String, String> mapping = new HashMap<String, String>() {{
        put("Phytoplankton", PHYTOPLANKTON);
        put("Zooplankton", ZOOPLANKTON);
        put("Bacterioplankton", BACTERIOPLANKTON);
        put("Plankton", PLANKTON);
        put("Invertebrata", INVERTEBRATA);
    }};

    private Map<String, String> pathLookup = new HashMap<String, String>() {{
        put(PHYTOPLANKTON, "Plankton | Phytoplankton");
        put(ZOOPLANKTON, "Plankton | Zooplankton | Animalia");
        put(BACTERIOPLANKTON, "Plankton | Bacterioplankton | Bacteria");
        put(PLANKTON, "Plankton");
        put(INVERTEBRATA, "Invertebrata");
    }};

    public String lookupIdByName(String taxonName) throws TaxonPropertyLookupServiceException {
        String id = null;
        String lowerCaseName = StringUtils.lowerCase(taxonName);
        if (StringUtils.isNotBlank(lowerCaseName)) {
            id = mapping.get(lowerCaseName);
        }
        return id;
    }

    @Override
    public String lookupTaxonPathById(String id) throws TaxonPropertyLookupServiceException {
        return pathLookup.get(id);
    }
}
