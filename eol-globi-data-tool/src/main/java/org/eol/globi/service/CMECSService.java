package org.eol.globi.service;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.JetFormat;
import com.healthmarketscience.jackcess.Table;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.eol.globi.domain.TaxonomyProvider;
import org.eol.globi.domain.Term;
import org.eol.globi.util.HttpUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMECSService implements TermLookupService {

    private static Log LOG = LogFactory.getLog(CMECSService.class);

    private Map<String, Term> termMap = null;

    @Override
    public List<Term> lookupTermByName(String name) throws TermLookupServiceException {
        if (termMap == null) {
            try {
                termMap = buildTermMap();
            } catch (IOException e) {
                throw new TermLookupServiceException("failed to instantiate terms", e);
            }
        }
        Term term = termMap.get(name);
        return term == null ? new ArrayList<Term>() : Arrays.asList(term);
    }

    private static Map<String, Term> buildTermMap() throws IOException {
        LOG.info(CMECSService.class.getSimpleName() + " instantiating...");
        String uri = "http://cmecscatalog.org/docs/cmecs4.accdb";
        LOG.info("CMECS data [" + uri + "] downloading ...");
        HttpResponse execute = HttpUtil.createHttpClient().execute(new HttpGet(uri));
        File cmecs = File.createTempFile("cmecs", "accdb");
        cmecs.deleteOnExit();
        IOUtils.copy(execute.getEntity().getContent(), new FileOutputStream(cmecs));
        LOG.info("CMECS data [" + uri + "] downloaded.");

        Database db = Database.open(new File(cmecs.toURI()), true);

        Map<String, Term> aquaticSettingsTerms = new HashMap<String, Term>();

        Table table = db.getTable("Aquatic Setting");
        Map<String, Object> row;
        while ((row = table.getNextRow()) != null) {
            Integer id = (Integer) row.get("AquaticSetting_Id");
            String name = (String) row.get("AquaticSettingName");
            String termId = TaxonomyProvider.ID_CMECS + id;
            aquaticSettingsTerms.put(name, new Term(termId, name));
        }
        cmecs.delete();
        LOG.info(CMECSService.class.getSimpleName() + " instantiated.");
        return aquaticSettingsTerms;
    }
}
