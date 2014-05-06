package org.eol.globi.export;

import org.apache.commons.lang.StringUtils;
import org.eol.globi.data.StudyImporterForGoMexSI;
import org.eol.globi.domain.Study;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.ResourceIterator;
import scala.collection.convert.Wrappers;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExporterGoMexSI implements StudyExporter {

    @Override
    public void exportStudy(final Study study, Writer writer, boolean includeHeader) throws IOException {
        if (StudyImporterForGoMexSI.GOMEXI_SOURCE_DESCRIPTION.equals(study.getSource())) {
            ExecutionEngine engine = new ExecutionEngine(study.getUnderlyingNode().getGraphDatabase());

            StringBuilder query = buildQuery();
            ExecutionResult result = engine.execute(query.toString(), new HashMap<String, Object>() {
                {
                    put("title", study.getTitle());
                }
            });

            if (includeHeader) {
                writer.write(StringUtils.join(result.columns().toArray(), ","));
            }

            ResourceIterator<Map<String, Object>> iterator = result.iterator();
            while (iterator.hasNext()) {
                writer.write("\n");
                Map<String, Object> row = iterator.next();
                boolean isFirstValue = true;
                for (String column : result.columns()) {
                    Object o = row.get(column);
                    if (o != null) {
                        if (!isFirstValue) {
                            writer.write(",");
                        }
                        if (o instanceof Collection) {
                            writer.write(StringUtils.join(((Collection) o).toArray(), "|"));
                        } else {
                            if (o instanceof String) {
                                writer.write("\"");
                            }
                            writer.append(o.toString());
                            if (o instanceof String) {
                                writer.write("\"");
                            }
                        }

                        isFirstValue = false;
                    }
                }
            }
            iterator.close();
        }
    }

    public static StringBuilder buildQuery() {
        StringBuilder query = new StringBuilder();
        query.append("START study = node:studies(title={title})");
        query.append(" MATCH study-[c:COLLECTED]->predator-->prey-[:CLASSIFIED_AS]->preyTaxon-[?:SAME_AS]->preyTaxonLinked");
        query.append(", predator-[:CLASSIFIED_AS]->predatorTaxon-[?:SAME_AS]->predatorTaxonLinked, predator-[:COLLECTED_AT]->loc");
        query.append(", loc-[?:IN_ECO_REGION]->ecoRegion");
        query.append(", loc-[?:HAS_ENVIRONMENT]->environment");
        query.append(" RETURN predatorTaxon.name as `predator taxon name`, collect(distinct(predatorTaxonLinked.externalId)) as `predator taxon ids`");
        query.append(", preyTaxon.name as `prey taxon name`, collect(distinct(preyTaxonLinked.externalId)) as `prey taxon id`");
        query.append(", c.time? as `time in unix epoch`, loc.latitude? as `latitude`, loc.longitude? as `longitude`, loc.altitude? as `depth(m)`");
        query.append(", collect(distinct(environment.name)) as `environment names`, collect(distinct(environment.externalId)) as `environment ids`");
        query.append(", collect(distinct(ecoRegion.name)) as `ecoRegion names`, collect(distinct(ecoRegion.externalId)) as `ecoRegion ids`");
        return query;
    }
}