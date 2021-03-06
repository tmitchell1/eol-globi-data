package org.eol.globi.service;

import java.io.IOException;

public interface DOIResolver {

    String findDOIForReference(final String reference) throws IOException;

    String findCitationForDOI(final String doi) throws IOException;

}
