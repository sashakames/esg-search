package esg.search.feed.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import com.sun.syndication.feed.rss.Category;
import com.sun.syndication.feed.rss.Description;
import com.sun.syndication.feed.rss.Enclosure;
import com.sun.syndication.feed.rss.Guid;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.feed.rss.Source;

import esg.search.core.Record;
import esg.search.publish.impl.RecordHelper;
import esg.search.publish.thredds.ThreddsPars;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Utility class for bulding RSS feeds.
 * 
 * @author Luca Cinquini
 */
public class RssViewBuilder {
    
    private static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static SimpleDateFormat df;
    
    private final static String FEED_TITLE_PROPERTY_KEY = "esg.feed.datasets.title";
    private final static String FEED_DESC_PROPERTY_KEY = "esg.feed.datasets.desc";
    private final static String FEED_LINK_PROPERTY_KEY = "esg.feed.datasets.link";
    
    static {
        // must set the time zone of the Date Formatter to GMT
        df = new SimpleDateFormat(DATE_FORMAT);
        //df.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
    }
    
    // time to live in minutes
    public static int TTL = 30;
    
    // ESGF namespace
    public final static String ESGF_NS = "http://www.esgf.org/cv/0.1/";
    
    // ESGF facets
    public final static String[] FACETS = new String[] { 
            "cf_variable", 
            "data_structure",
            "experiment",
            "experiment_family",
            "institute",
            "model",
            "processing_level",
            "product",
            "project",
            "realm",
            "source_id",
            "time_frequency",
            "tracking_id",
            "variable"
    };
    
    /**
     * <enclosure url="http://esg-datanode.jpl.nasa.gov/thredds/fileServer/esg_dataroot/obs4MIPs/observations/atmos/husNobs/mon/grid/NASA-JPL/AIRS/v20110608/husNobs_AIRS_L3_RetStd-v5_200209-201105.nc" type="application/x-netcdf" />
     * <enclosure url="http://esg-datanode.jpl.nasa.gov/thredds/dodsC/esg_dataroot/obs4MIPs/observations/atmos/husNobs/mon/grid/NASA-JPL/AIRS/v20110608/husNobs_AIRS_L3_RetStd-v5_200209-201105.nc.html" type="text/html" />
     */
    public final static void addEnclosures(Item feedItem, Record record) throws Exception {
        
        final List<Enclosure> enclosures = new ArrayList<Enclosure>();
        
        // loop over record access services
        for (String service : record.getFieldValues(SolrXmlPars.FIELD_SERVICE)) {
            
            // service type, service description, service url
            final String[] _service = RecordHelper.decodeServiceField(service);
            
            Enclosure enc = new Enclosure();
            enc.setUrl(_service[2]);
            if ( _service[0].equals(ThreddsPars.SERVICE_TYPE_HTTP)) {
                if (_service[2].endsWith(".nc") || _service[2].endsWith(".cdf")) {
                    enc.setType(FeedController.MIME_TYPE_NETCDF);
                } else if (_service[2].endsWith(".hdf")) {
                    enc.setType(FeedController.MIME_TYPE_HDF);
                } else {
                    enc.setType(FeedController.MIME_TYPE_HTML);
                }
            } else if (_service[0].equals(ThreddsPars.SERVICE_TYPE_OPENDAP)) {
                enc.setUrl( _service[2]+".dods" ); // filename.nc.dods
                enc.setType(FeedController.MIME_TYPE_DODS);
            //} else if (_service[0].equals(ThreddsPars.SERVICE_TYPE_OPENDAP)) {
            //    enc.setUrl( _service[2]+".html" ); // filename.nc.html
            //    enc.setType(FeedController.MIME_TYPE_HTML);
            } else {
                enc.setType(FeedController.MIME_TYPE_HTML);
            }
            enclosures.add(enc);
        }
        feedItem.setEnclosures(enclosures);

    }
    
    // <description>MODSCW_P2011192_C4_1750_1755_1930_1935_GL05_closest_chlora.hdf</description>
    public final static void addDescription(Item feedItem, Record record) throws Exception {
        String description = record.getFieldValue(SolrXmlPars.FIELD_DESCRIPTION);
        // use record ID if description is not found
        if (!StringUtils.hasText(description)) description = record.getId();
        Description desc = new Description();  
        desc.setType("text/plain");  
        desc.setValue(description);  
        feedItem.setDescription(desc);                    
    }
    
    // <pubDate>Wed, 24 Aug 2011 16:43:47 GMT</pubDate>
    public final static void addPubDate(Item feedItem, Record record) throws Exception {      
        // replace Zulu time with GMT time zone
        String date = record.getFieldValue(SolrXmlPars.FIELD_TIMESTAMP).replace("Z", "+0000");
        feedItem.setPubDate( df.parse( date )); // result is on locale time
    }

    // <guid isPermaLink="true">http://coastwatch.noaa.gov/thredds/fileServer/chloraAquaMODISDailyCWHDFGL05/MODSCW_P2011189_C4_1720_1725_1900_1905_GL05_closest_chlora.hdf</guid>
    public final static void addGuid(Item feedItem, Record record) {
        Guid guid = new Guid();
        guid.setValue(record.getId());
        guid.setPermaLink(false); // NOT a URL
        feedItem.setGuid( guid );
    }
    
    //  <source url="http://coastwatch.noaa.gov/thredds/coastwatch/modis/chlora/dynamic/catalog_modis_chlora_daily_GL05.html">
    //          Great Lakes Mercator Chlorophyll-a Daily Merge, Aqua MODIS</source>
    public final static void addSource(Item feedItem, String title, String url) {
        Source source = new Source();
        source.setValue( title );
        source.setUrl( url );
        feedItem.setSource( source ); 
    }
    
    // <category domain="http://www.esgf.org/cv/0.1/experiment">obs</category>
    public final static void addCategories(Item feedItem, Record record) {
      
        final List<Category> categories = new ArrayList<Category>();
        
        // loop over predefined facets
        for (String key : FACETS) {
            for (String value : record.getFieldValues(key)) {
                Category category = new Category();
                category.setDomain(ESGF_NS+key);
                category.setValue(value);
                categories.add(category);
            }
        }
        feedItem.setCategories(categories);
        
    }
    
    public final static String getRssBaseUri(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        return url.substring(0, url.lastIndexOf("/")+1);
    }
    
    public final static String getFeedTitle(Properties properties) {
        if (StringUtils.hasText(properties.getProperty(FEED_TITLE_PROPERTY_KEY))) {
            return properties.getProperty(FEED_TITLE_PROPERTY_KEY);  
        } else {
            return "ESGF Node RSS Feed";
        }
    }
    
    public final static String getFeedDesc(Properties properties) {
        if (StringUtils.hasText(properties.getProperty(FEED_DESC_PROPERTY_KEY))) {
            return properties.getProperty(FEED_DESC_PROPERTY_KEY);  
        } else {
            return "ESGF Node Datasets List";
        }
    }
    
    public final static String getFeedLink(Properties properties, HttpServletRequest request) {
        if (StringUtils.hasText(properties.getProperty(FEED_LINK_PROPERTY_KEY))) {
            return properties.getProperty(FEED_LINK_PROPERTY_KEY);  
        } else {
            return "http://"+request.getServerName()+"/thredds/catalog.html";
        }

    }
}
