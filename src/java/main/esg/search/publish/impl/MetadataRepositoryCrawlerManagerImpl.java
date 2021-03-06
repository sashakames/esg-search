/*******************************************************************************
 * Copyright (c) 2010 Earth System Grid Federation
 * ALL RIGHTS RESERVED. 
 * U.S. Government sponsorship acknowledged.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package esg.search.publish.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import esg.search.publish.api.MetadataRepositoryCrawler;
import esg.search.publish.api.MetadataRepositoryCrawlerManager;
import esg.search.publish.api.MetadataRepositoryType;

/**
 * Service class that manages the harvesting of search records from different remote metadata repositories.
 * Note that the the specific MetadataRepositoryCrawlers are mapped to the corresponding metadata repository type
 * through their implementation of the support() method.
 */
public class MetadataRepositoryCrawlerManagerImpl extends RecordProducerImpl implements MetadataRepositoryCrawlerManager {
	
	private Map<MetadataRepositoryType, MetadataRepositoryCrawler> crawlers = new HashMap<MetadataRepositoryType, MetadataRepositoryCrawler>();
		
	private static final Log LOG = LogFactory.getLog(MetadataRepositoryCrawlerManagerImpl.class);
	
	
	public MetadataRepositoryCrawlerManagerImpl(final MetadataRepositoryCrawler[] _crawlers) {
		for (final MetadataRepositoryCrawler crawler : _crawlers) {
			crawlers.put(crawler.supports(), crawler);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void crawl(final String uri, final String filter, boolean recursive, final MetadataRepositoryType metadataRepositoryType, boolean publish, URI schema) throws Exception {
		
		if (LOG.isInfoEnabled()) LOG.info("uri="+uri+"filter="+filter+" recursive="+recursive+" metadataRepositoryType="+metadataRepositoryType);
		MetadataRepositoryCrawler crawler = crawlers.get(metadataRepositoryType);
		
		Assert.notNull(crawler, "Unsupported MetadataRepositoryType:"+metadataRepositoryType);
		crawler.crawl(new URI(uri), filter, recursive, this, publish, schema);
		
	}

}
