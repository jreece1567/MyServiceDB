/**
 *
 */
package com.myservicedb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.westfieldlabs.restsdk.internals.RestSdkException;
import com.westfieldlabs.restsdk.model.centre.CentreInstance;
import com.westfieldlabs.restsdk.model.centre.CentreListInstance;
import com.westfieldlabs.restsdk.model.centre.CentreListResponse;
import com.westfieldlabs.restsdk.model.centre.NoticeInstance;
import com.westfieldlabs.restsdk.model.centre.NoticeListInstance;
import com.westfieldlabs.restsdk.model.centre.NoticeListResponse;
import com.westfieldlabs.restsdk.model.centre_directory.Service;
import com.westfieldlabs.restsdk.model.centre_directory.ServicesListResponse;
import com.westfieldlabs.restsdk.model.collection.CurationInstance;
import com.westfieldlabs.restsdk.model.collection.CurationListInstance;
import com.westfieldlabs.restsdk.model.collection.CurationsListResponse;
import com.westfieldlabs.restsdk.model.deal.DealInstance;
import com.westfieldlabs.restsdk.model.deal.DealsListResponse;
import com.westfieldlabs.restsdk.model.event.EventInstance;
import com.westfieldlabs.restsdk.model.event.EventsListResponse;
import com.westfieldlabs.restsdk.model.movie.MovieInstance;
import com.westfieldlabs.restsdk.model.movie.MovieListInstance;
import com.westfieldlabs.restsdk.model.movie.MoviesListResponse;
import com.westfieldlabs.restsdk.model.store.RetailerInstance;
import com.westfieldlabs.restsdk.model.store.RetailersListResponse;
import com.westfieldlabs.restsdk.model.store.StoreInstance;
import com.westfieldlabs.restsdk.model.store.StoresListResponse;
import com.westfieldlabs.restsdk.services.RestSdkApiService;
import com.westfieldlabs.restsdk.services.RestSdkApiServiceBuilder;

/**
 * @author jreece
 *
 */
public class Main {

    /**
     * The API-key, obtained from the WestfieldLabs 'API' team.
     * <p>
     * &nbsp;
     * </p>
     * <b>Note:</b> The API-key hard-coded into this sample application is not
     * valid and will not work.
     */
    private static String API_KEY = "123456789012345678901234";

    /**
     * A shared Gson instance
     */
    private final Gson gson = new GsonBuilder().setPrettyPrinting()
            .disableHtmlEscaping().serializeNulls().create();

    /**
     * The WestfieldLabs REST API service-facade.
     */
    private final RestSdkApiService service;

    /**
     * The DB file-path
     */
    private static String FILE_PATH = "";

    /**
     * A map of country-code --> array of CentreListInstance objects
     */
    private JsonObject centreDB = null;

    /**
     * A map of centre_id --> array of Service objects
     */
    private JsonObject centreDirectoryDB = null;

    /**
     * A map of centre_id --> array of NoticeListInstance objects
     */
    private JsonObject noticesDB = null;

    /**
     * A map of centre_id --> array of EventInstance objects
     */
    private JsonObject eventsDB = null;

    /**
     * A map of centre_id --> array of DealInstance objects
     */
    private JsonObject dealsDB = null;

    /**
     * A map of country-code --> array of CurationListInstance objects
     */
    private JsonObject curationsDB = null;

    /**
     * A map of country-code --> array of RetailerInstance objects
     */
    private JsonObject retailersDB = null;

    /**
     * A map of country-code --> array of StoreInstance objects
     */
    private JsonObject storesDB = null;

    /**
     * A map of centre_id --> array of MovieListInstance objects
     */
    private JsonObject moviesDB = null;

    /**
     * The lists of 'null' or 'empty-string' attribute-names. (the only useful
     * info in these are in the map-'keys'). We use Map to ensure unique
     * entries, and use TreeMap to maintain the keys in 'natural order'.
     */
    private final Map<String, String> nullMap = new TreeMap<String, String>();
    private final Map<String, String> emptyMap = new TreeMap<String, String>();

    /**
     * Constructor
     */
    public Main() {

        // create a service-facade, using the API key obtained from
        // Westfield. Take the defaults for all other configurable features.
        this.service = (new RestSdkApiServiceBuilder.Builder()).setApiKey(
                API_KEY).build();
    }

    private final String getFilename(final DB db) {
        return FILE_PATH + db.getValue() + ".json";
    }

    private final String getNullFilename(final DB db) {
        return FILE_PATH + db.getValue() + "_nulls" + ".json";
    }

    private final String getEmptyFilename(final DB db) {
        return FILE_PATH + db.getValue() + "_empties" + ".json";
    }

    private void findNulls(final String name, final JsonArray jsonArray) {

        for (int i = 0; i < jsonArray.size(); i++) {
            final JsonElement element = jsonArray.get(i);

            if (element.isJsonArray()) {

                findNulls(name, element.getAsJsonArray());

            } else if (element.isJsonObject()) {

                findNulls(name, element.getAsJsonObject());
            }
        }
    }

    private void findNulls(final String name, final JsonObject jsonObject) {

        final Iterator<Entry<String, JsonElement>> it = jsonObject.entrySet()
                .iterator();
        while (it.hasNext()) {
            final Entry<String, JsonElement> entry = it.next();

            if (entry.getValue().isJsonArray()) {

                findNulls(name, entry.getValue().getAsJsonArray());

            } else if (entry.getValue().isJsonObject()) {

                findNulls(name + "." + entry.getKey(), entry.getValue()
                        .getAsJsonObject());

            } else if (entry.getValue().isJsonNull()) {

                // note that we found a null
                final String nullName = name + "." + entry.getKey();
                nullMap.put(nullName, "");

            } else if ("\"\"".equals(entry.getValue().toString())) {

                // note that we found an empty string
                final String nullName = name + "." + entry.getKey();
                emptyMap.put(nullName, "");
            }
        }

    }

    private JsonArray centresByCountry(final String country) {

        final JsonArray centres = new JsonArray();
        final List<String> statuses = Arrays.asList("live", "pending",
                "preview");
        ;

        int page = 1;
        CentreListResponse response = service.centre.getCentres(null, country,
                null, null, null, null, page, 100, null, statuses, null);
        for (final CentreListInstance centre : response.getData()) {
            centres.add(new JsonParser().parse(gson.toJson(centre,
                    CentreListInstance.class)));
        }

        final int pageCount = response.getMeta().getPage_count();
        while (pageCount > page) {
            page = page + 1;
            response = service.centre.getCentres(null, country, null, null,
                    null, null, page, 100, null, statuses, null);
            for (final CentreListInstance centre : response.getData()) {
                centres.add(new JsonParser().parse(gson.toJson(centre,
                        CentreListInstance.class)));
            }
        }

        findNulls("CentreListInstance", centres);

        return centres;
    }

    private JsonObject getCentreDB() {
        final JsonObject centreDB = new JsonObject();

        centreDB.add("AU", centresByCountry("AU"));
        centreDB.add("NZ", centresByCountry("NZ"));
        centreDB.add("US", centresByCountry("US"));
        centreDB.add("UK", centresByCountry("UK"));

        return centreDB;
    }

    private JsonArray servicesByCentre(final String centreId) {
        final JsonArray centreServices = new JsonArray();

        {
            final String serviceClass = "physical";

            int page = 1;
            ServicesListResponse response = service.centre_directory
                    .getCentreServices(centreId, null, null, page, 100,
                            serviceClass, null);
            for (final Service service : response.getData()) {
                centreServices.add(new JsonParser().parse(gson.toJson(service,
                        Service.class)));
            }

            final int pageCount = response.getMeta().getPage_count();
            while (pageCount > page) {
                page = page + 1;
                response = service.centre_directory.getCentreServices(centreId,
                        null, null, page, 100, serviceClass, null);
                for (final Service service : response.getData()) {
                    centreServices.add(new JsonParser().parse(gson.toJson(
                            service, Service.class)));
                }
            }
        }

        {
            final String serviceClass = "digital";

            int page = 1;
            ServicesListResponse response = service.centre_directory
                    .getCentreServices(centreId, null, null, page, 100,
                            serviceClass, null);
            for (final Service service : response.getData()) {
                centreServices.add(new JsonParser().parse(gson.toJson(service,
                        Service.class)));
            }

            final int pageCount = response.getMeta().getPage_count();
            while (pageCount > page) {
                page = page + 1;
                response = service.centre_directory.getCentreServices(centreId,
                        null, null, page, 100, serviceClass, null);
                for (final Service service : response.getData()) {
                    centreServices.add(new JsonParser().parse(gson.toJson(
                            service, Service.class)));
                }
            }
        }

        findNulls("Service", centreServices);

        return centreServices;
    }

    private JsonObject getCentreDirectoryDB(final JsonObject centreDB) {
        final JsonObject centreDirectoryDB = new JsonObject();

        final Iterator<Entry<String, JsonElement>> itCentre = centreDB
                .entrySet().iterator();
        while (itCentre.hasNext()) {
            final JsonArray centres = itCentre.next().getValue()
                    .getAsJsonArray();
            for (int i = 0; i < centres.size(); i++) {
                final JsonObject centre = centres.get(i).getAsJsonObject();
                final String centre_id = centre.get("centre_id").getAsString();
                centreDirectoryDB.add(centre_id, servicesByCentre(centre_id));
            }
        }

        return centreDirectoryDB;
    }

    private JsonArray noticesByCentre(final String centreId) {
        final JsonArray centreNotices = new JsonArray();
        final List<String> statuses = Arrays.asList("live", "pending",
                "preview");
        ;

        int page = 1;
        NoticeListResponse response = service.centre.getNotices(centreId, null,
                null, page, 100, null, null, statuses, null, null);
        for (final NoticeListInstance notice : response.getData()) {
            centreNotices.add(new JsonParser().parse(gson.toJson(notice,
                    NoticeListInstance.class)));
        }

        final int pageCount = response.getMeta().getPage_count();
        while (pageCount > page) {
            page = page + 1;
            response = service.centre.getNotices(centreId, null, null, page,
                    100, null, null, statuses, null, null);
            for (final NoticeListInstance notice : response.getData()) {
                centreNotices.add(new JsonParser().parse(gson.toJson(notice,
                        NoticeListInstance.class)));
            }
        }

        findNulls("NoticeListInstance", centreNotices);

        return centreNotices;
    }

    private JsonObject getCentreNoticesDB(final JsonObject centreDB) {
        final JsonObject centreNoticesDB = new JsonObject();

        final Iterator<Entry<String, JsonElement>> itCentre = centreDB
                .entrySet().iterator();
        while (itCentre.hasNext()) {
            final JsonArray centres = itCentre.next().getValue()
                    .getAsJsonArray();
            for (int i = 0; i < centres.size(); i++) {
                final JsonObject centre = centres.get(i).getAsJsonObject();
                final String centre_id = centre.get("centre_id").getAsString();
                centreNoticesDB.add(centre_id, noticesByCentre(centre_id));
            }
        }

        return centreNoticesDB;
    }

    private JsonArray eventsByCentre(final String centreId) {
        final JsonArray centreEvents = new JsonArray();
        final List<String> statuses = Arrays.asList("live", "pending",
                "preview");
        ;

        int page = 1;
        EventsListResponse response = service.event.getEvents(null, centreId,
                null, null, null, null, page, 100, null, null, null, null,
                statuses, null);
        for (final EventInstance event : response.getData()) {
            centreEvents.add(new JsonParser().parse(gson.toJson(event,
                    EventInstance.class)));
        }

        final int pageCount = response.getMeta().getPage_count();
        while (pageCount > page) {
            page = page + 1;
            response = service.event.getEvents(null, centreId, null, null,
                    null, null, page, 100, null, null, null, null, statuses,
                    null);
            for (final EventInstance event : response.getData()) {
                centreEvents.add(new JsonParser().parse(gson.toJson(event,
                        EventInstance.class)));
            }
        }

        findNulls("EventInstance", centreEvents);

        return centreEvents;
    }

    private JsonObject getCentreEventsDB(final JsonObject centreDB) {
        final JsonObject centreEventsDB = new JsonObject();

        final Iterator<Entry<String, JsonElement>> itCentre = centreDB
                .entrySet().iterator();
        while (itCentre.hasNext()) {
            final JsonArray centres = itCentre.next().getValue()
                    .getAsJsonArray();
            for (int i = 0; i < centres.size(); i++) {
                final JsonObject centre = centres.get(i).getAsJsonObject();
                final String centre_id = centre.get("centre_id").getAsString();
                centreEventsDB.add(centre_id, eventsByCentre(centre_id));
            }
        }

        return centreEventsDB;
    }

    private JsonArray dealsByCentre(final String centreId) {
        final JsonArray centreDeals = new JsonArray();
        final List<String> statuses = Arrays.asList("live", "pending",
                "preview");
        ;

        int page = 1;
        DealsListResponse response = service.deal.getDeals(null, null, null,
                centreId, null, null, null, null, null, page, 100, null, null,
                null, statuses, null, null);
        for (final DealInstance deal : response.getData()) {
            centreDeals.add(new JsonParser().parse(gson.toJson(deal,
                    DealInstance.class)));
        }

        final int pageCount = response.getMeta().getPage_count();
        while (pageCount > page) {
            page = page + 1;
            response = service.deal.getDeals(null, null, null, centreId, null,
                    null, null, null, null, page, 100, null, null, null,
                    statuses, null, null);
            for (final DealInstance deal : response.getData()) {
                centreDeals.add(new JsonParser().parse(gson.toJson(deal,
                        DealInstance.class)));
            }
        }

        findNulls("DealInstance", centreDeals);

        return centreDeals;
    }

    private JsonObject getCentreDealsDB(final JsonObject centreDB) {
        final JsonObject centreDealsDB = new JsonObject();

        final Iterator<Entry<String, JsonElement>> itCentre = centreDB
                .entrySet().iterator();
        while (itCentre.hasNext()) {
            final JsonArray centres = itCentre.next().getValue()
                    .getAsJsonArray();
            for (int i = 0; i < centres.size(); i++) {
                final JsonObject centre = centres.get(i).getAsJsonObject();
                final String centre_id = centre.get("centre_id").getAsString();
                centreDealsDB.add(centre_id, dealsByCentre(centre_id));
            }
        }

        return centreDealsDB;
    }

    private JsonArray curationsByCountry(final String country) {
        final JsonArray curations = new JsonArray();
        final List<String> statuses = Arrays.asList("live", "pending",
                "preview");
        ;

        int page = 1;
        CurationsListResponse response = service.collection.getCurations(null,
                null, country, null, null, null, null, page, 100, null, null,
                null, statuses);
        for (final CurationListInstance curation : response.getData()) {
            curations.add(new JsonParser().parse(gson.toJson(curation,
                    CurationListInstance.class)));
        }

        final int pageCount = response.getMeta().getPage_count();
        while (pageCount > page) {
            page = page + 1;
            response = service.collection.getCurations(null, null, country,
                    null, null, null, null, page, 100, null, null, null,
                    statuses);
            for (final CurationListInstance curation : response.getData()) {
                curations.add(new JsonParser().parse(gson.toJson(curation,
                        CurationListInstance.class)));
            }
        }

        findNulls("CurationListInstance", curations);

        return curations;
    }

    private JsonObject getCurationsDB() {
        final JsonObject curationsDB = new JsonObject();

        curationsDB.add("AU", curationsByCountry("au"));
        curationsDB.add("NZ", curationsByCountry("nz"));
        curationsDB.add("US", curationsByCountry("us"));
        curationsDB.add("UK", curationsByCountry("uk"));

        return curationsDB;
    }

    private JsonArray retailersByCountry(final String country) {
        final JsonArray retailers = new JsonArray();
        final List<String> statuses = Arrays.asList("live", "pending",
                "preview");
        ;

        int page = 1;
        RetailersListResponse response = service.store.getRetailers(null, null,
                null, country, null, null, null, null, null, page, 100, null,
                null, null, statuses, null);
        for (final RetailerInstance retailer : response.getData()) {
            retailers.add(new JsonParser().parse(gson.toJson(retailer,
                    RetailerInstance.class)));
        }

        final int pageCount = response.getMeta().getPage_count();
        while (pageCount > page) {
            page = page + 1;
            response = service.store.getRetailers(null, null, null, country,
                    null, null, null, null, null, page, 100, null, null, null,
                    statuses, null);
            for (final RetailerInstance retailer : response.getData()) {
                retailers.add(new JsonParser().parse(gson.toJson(retailer,
                        RetailerInstance.class)));
            }
        }

        findNulls("RetailerInstance", retailers);

        return retailers;
    }

    private JsonObject getRetailersDB() {
        final JsonObject retailersDB = new JsonObject();

        retailersDB.add("AU", retailersByCountry("au"));
        retailersDB.add("NZ", retailersByCountry("nz"));
        retailersDB.add("US", retailersByCountry("us"));
        retailersDB.add("UK", retailersByCountry("uk"));

        return retailersDB;
    }

    private JsonArray storesByCountry(final String country) {
        final JsonArray stores = new JsonArray();
        final List<String> statuses = Arrays.asList("live", "pending",
                "preview");
        ;

        int page = 1;
        StoresListResponse response = service.store.getStores(null, null,
                country, null, null, null, page, 100, null, null, null, null,
                statuses, null, null, null, null);
        for (final StoreInstance store : response.getData()) {
            stores.add(new JsonParser().parse(gson.toJson(store,
                    StoreInstance.class)));
        }

        final int pageCount = response.getMeta().getPage_count();
        while (pageCount > page) {
            page = page + 1;
            response = service.store.getStores(null, null, country, null, null,
                    null, page, 100, null, null, null, null, statuses, null,
                    null, null, null);
            for (final StoreInstance store : response.getData()) {
                stores.add(new JsonParser().parse(gson.toJson(store,
                        StoreInstance.class)));
            }
        }

        findNulls("StoreInstance", stores);

        return stores;
    }

    private JsonObject getStoresDB() {
        final JsonObject storesDB = new JsonObject();

        storesDB.add("AU", storesByCountry("au"));
        storesDB.add("NZ", storesByCountry("nz"));
        storesDB.add("US", storesByCountry("us"));
        storesDB.add("UK", storesByCountry("uk"));

        return storesDB;
    }

    private JsonArray moviesByCentre(final String centreId) {
        final JsonArray centreMovies = new JsonArray();

        final Date startDate = new Date();
        final Date endDate = new Date(startDate.getTime()
                + TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS));

        int page = 1;
        MoviesListResponse response = service.movie.getMovies(centreId, null,
                null, endDate, null, page, 100, startDate, null);
        for (final MovieListInstance Movie : response.getData()) {
            centreMovies.add(new JsonParser().parse(gson.toJson(Movie,
                    MovieListInstance.class)));
        }

        final int pageCount = response.getMeta().getPage_count();
        while (pageCount > page) {
            page = page + 1;
            response = service.movie.getMovies(centreId, null, null, endDate,
                    null, page, 100, startDate, null);
            for (final MovieListInstance Movie : response.getData()) {
                centreMovies.add(new JsonParser().parse(gson.toJson(Movie,
                        MovieListInstance.class)));
            }
        }

        findNulls("movieListInstance", centreMovies);

        return centreMovies;
    }

    private JsonObject getCentreMoviesDB(final JsonObject centreDB) {
        final JsonObject centreMoviesDB = new JsonObject();

        final Iterator<Entry<String, JsonElement>> itCentre = centreDB
                .entrySet().iterator();
        while (itCentre.hasNext()) {
            final JsonArray centres = itCentre.next().getValue()
                    .getAsJsonArray();
            for (int i = 0; i < centres.size(); i++) {
                final JsonObject centre = centres.get(i).getAsJsonObject();
                final String centre_id = centre.get("centre_id").getAsString();
                centreMoviesDB.add(centre_id, moviesByCentre(centre_id));
            }
        }

        return centreMoviesDB;
    }

    private JsonObject readDB(final DB db) {
        final String str = FileUtils.readFileAsString(getFilename(db));
        if (str != null) {
            return new JsonParser().parse(str).getAsJsonObject();
        } else {
            System.out.println("File " + getFilename(db) + " is empty.");
            return null;
        }
    }

    private void readDBs(final DB db) {

        List<DB> dbs = new ArrayList<DB>();

        if (DB.ALL.equals(db)) {
            dbs = Arrays.asList(DB.values());
        } else {
            dbs.add(db);
        }
        for (final DB dbType : dbs) {

            switch (dbType) {

                case CENTRE:
                    this.centreDB = readDB(DB.CENTRE);
                    break;

                case CENTRE_DIRECTORY:
                    this.centreDirectoryDB = readDB(DB.CENTRE_DIRECTORY);
                    break;

                case NOTICE:
                    this.noticesDB = readDB(DB.NOTICE);
                    break;

                case EVENT:
                    this.eventsDB = readDB(DB.EVENT);
                    break;

                case DEAL:
                    this.dealsDB = readDB(DB.DEAL);
                    break;

                case COLLECTION:
                    this.curationsDB = readDB(DB.COLLECTION);
                    break;

                case RETAILER:
                    this.retailersDB = readDB(DB.RETAILER);
                    break;

                case STORE:
                    this.storesDB = readDB(DB.STORE);
                    break;

                case MOVIE:
                    this.moviesDB = readDB(DB.MOVIE);
                    break;

                case ALL:
                    break;

                default:
                    break;
            }
        }
    }

    private void writeDB(final DB db, final JsonObject dbObj) {
        if (dbObj != null) {
            FileUtils.writeFileFromString(getFilename(db), gson.toJson(dbObj));
            FileUtils.writeFileFromString(getNullFilename(db),
                    gson.toJson(nullMap.keySet()));
            FileUtils.writeFileFromString(getEmptyFilename(db),
                    gson.toJson(emptyMap.keySet()));
        }
    }

    private void writeDBs(final DB db) {

        List<DB> dbs = new ArrayList<DB>();

        if (DB.ALL.equals(db)) {
            dbs = Arrays.asList(DB.values());
        } else {
            dbs.add(db);
        }
        for (final DB dbType : dbs) {

            switch (dbType) {

                case CENTRE:
                    writeDB(DB.CENTRE, this.centreDB);
                    break;

                case CENTRE_DIRECTORY:
                    writeDB(DB.CENTRE_DIRECTORY, this.centreDirectoryDB);
                    break;

                case NOTICE:
                    writeDB(DB.NOTICE, this.noticesDB);
                    break;

                case EVENT:
                    writeDB(DB.EVENT, this.eventsDB);
                    break;

                case DEAL:
                    writeDB(DB.DEAL, this.dealsDB);
                    break;

                case COLLECTION:
                    writeDB(DB.COLLECTION, this.curationsDB);
                    break;

                case RETAILER:
                    writeDB(DB.RETAILER, this.retailersDB);
                    break;

                case STORE:
                    writeDB(DB.STORE, this.storesDB);
                    break;

                case MOVIE:
                    writeDB(DB.MOVIE, this.moviesDB);
                    break;

                case ALL:
                    break;

                default:
                    break;
            }
        }
    }

    private void createDBs(final DB db) {

        List<DB> dbs = new ArrayList<DB>();

        if (DB.ALL.equals(db)) {
            dbs = Arrays.asList(DB.values());
        } else {
            dbs.add(db);
        }

        for (final DB dbType : dbs) {

            switch (dbType) {

                case CENTRE:
                    nullMap.clear();
                    emptyMap.clear();
                    this.centreDB = getCentreDB();
                    getCentreDetails();
                    writeDBs(DB.CENTRE);
                    break;

                case CENTRE_DIRECTORY:
                    if (this.centreDB == null) {
                        this.centreDB = getCentreDB();
                    }
                    nullMap.clear();
                    emptyMap.clear();
                    this.centreDirectoryDB = getCentreDirectoryDB(this.centreDB);
                    writeDBs(DB.CENTRE_DIRECTORY);
                    break;

                case NOTICE:
                    if (this.centreDB == null) {
                        this.centreDB = getCentreDB();
                    }
                    nullMap.clear();
                    emptyMap.clear();
                    this.noticesDB = getCentreNoticesDB(this.centreDB);
                    getNoticeDetails();
                    writeDBs(DB.NOTICE);
                    break;

                case EVENT:
                    if (this.centreDB == null) {
                        this.centreDB = getCentreDB();
                    }
                    nullMap.clear();
                    emptyMap.clear();
                    this.eventsDB = getCentreEventsDB(this.centreDB);
                    writeDBs(DB.EVENT);
                    break;

                case DEAL:
                    if (this.centreDB == null) {
                        this.centreDB = getCentreDB();
                    }
                    nullMap.clear();
                    emptyMap.clear();
                    this.dealsDB = getCentreDealsDB(this.centreDB);
                    writeDBs(DB.DEAL);
                    break;

                case COLLECTION:
                    nullMap.clear();
                    emptyMap.clear();
                    this.curationsDB = getCurationsDB();
                    getCollectionDetails();
                    writeDBs(DB.COLLECTION);
                    break;

                case RETAILER:
                    nullMap.clear();
                    emptyMap.clear();
                    this.retailersDB = getRetailersDB();
                    writeDBs(DB.RETAILER);
                    break;

                case STORE:
                    nullMap.clear();
                    emptyMap.clear();
                    this.storesDB = getStoresDB();
                    writeDBs(DB.STORE);
                    break;

                case MOVIE:
                    if (this.centreDB == null) {
                        this.centreDB = getCentreDB();
                    }
                    nullMap.clear();
                    emptyMap.clear();
                    this.moviesDB = getCentreMoviesDB(this.centreDB);
                    getMovieDetails();
                    writeDBs(DB.MOVIE);
                    break;

                case ALL:
                    break;

                default:
                    break;

            }
        }
    }

    private void getCollectionDetailsByCountry(final String country) {
        final JsonArray db = this.curationsDB.get(country).getAsJsonArray();

        for (int i = 0; i < db.size(); i++) {
            final JsonObject collection = db.get(i).getAsJsonObject();
            final CurationInstance instance = service.collection.getCuration(
                    collection.get("curation_id").getAsInt()).getData();
            findNulls(
                    "CollectionInstance",
                    new JsonParser().parse(
                            gson.toJson(instance, CurationInstance.class))
                            .getAsJsonObject());
        }
    }

    private void getCollectionDetails() {

        getCollectionDetailsByCountry("AU");
        getCollectionDetailsByCountry("NZ");
        getCollectionDetailsByCountry("US");
        getCollectionDetailsByCountry("UK");

    }

    private void getCentreDetailsByCountry(final String country) {
        final JsonArray db = this.centreDB.get(country).getAsJsonArray();

        for (int i = 0; i < db.size(); i++) {
            final JsonObject centre = db.get(i).getAsJsonObject();
            final CentreInstance instance = service.centre.getCentre(
                    centre.get("centre_id").getAsString(), null).getData();
            findNulls(
                    "CentreInstance",
                    new JsonParser().parse(
                            gson.toJson(instance, CentreInstance.class))
                            .getAsJsonObject());
        }
    }

    private void getCentreDetails() {

        getCentreDetailsByCountry("AU");
        getCentreDetailsByCountry("NZ");
        getCentreDetailsByCountry("US");
        getCentreDetailsByCountry("UK");

    }

    private void getNoticeDetails() {

        final Iterator<Entry<String, JsonElement>> it = this.noticesDB
                .entrySet().iterator();
        while (it.hasNext()) {
            final Entry<String, JsonElement> entry = it.next();
            final JsonArray notices = entry.getValue().getAsJsonArray();
            for (int i = 0; i < notices.size(); i++) {
                final NoticeInstance instance = service.centre.getNotice(
                        null,
                        notices.get(i).getAsJsonObject().get("notice_id")
                                .getAsInt()).getData();
                findNulls(
                        "NoticeInstance",
                        new JsonParser().parse(
                                gson.toJson(instance, NoticeInstance.class))
                                .getAsJsonObject());
            }
        }

    }

    private void getMovieDetails() {

        final Iterator<Entry<String, JsonElement>> it = this.moviesDB
                .entrySet().iterator();
        while (it.hasNext()) {
            final Entry<String, JsonElement> entry = it.next();
            final JsonArray movies = entry.getValue().getAsJsonArray();
            for (int i = 0; i < movies.size(); i++) {
                try {
                    final MovieInstance instance = service.movie.getMovie(
                            entry.getKey(),
                            null,
                            null,
                            movies.get(i).getAsJsonObject().get("movie_id")
                                    .getAsLong(), null).getData();
                    findNulls(
                            "MovieInstance",
                            new JsonParser().parse(
                                    gson.toJson(instance, MovieInstance.class))
                                    .getAsJsonObject());
                } catch (final RestSdkException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

    }

    private void usage() {
        System.out
                .println("Usage: java -jar MyServiceDB.jar <api_key> <output-file_path> [ all | "
                        + DB.getValues().toString() + " ]");
    }

    private void countEntriesByCountry(final JsonObject db) {
        final Iterator<Entry<String, JsonElement>> itCountries = db.entrySet()
                .iterator();
        while (itCountries.hasNext()) {
            final Entry<String, JsonElement> entry = itCountries.next();
            final JsonArray items = entry.getValue().getAsJsonArray();
            System.out.println("Key: " + entry.getKey() + ", entries="
                    + items.size());
        }
    }

    private void countEntriesByCentre(final JsonObject db) {
        int totalItems = 0;
        final Iterator<Entry<String, JsonElement>> itCentres = db.entrySet()
                .iterator();
        while (itCentres.hasNext()) {
            final Entry<String, JsonElement> entry = itCentres.next();
            final JsonArray items = entry.getValue().getAsJsonArray();
            System.out.println("Key: " + entry.getKey() + ", entries="
                    + items.size());
            totalItems = totalItems + items.size();
        }
        System.out.println("Total items: " + totalItems);
    }

    /**
     * @param args
     */
    private void instanceMainCreate(final String[] args) {
        usage();
        System.out.println("Parameters: " + Arrays.asList(args));
        if (args.length > 3) {
            int ix = 3;
            while (ix < args.length) {
                DB db;
                if ((db = DB.forValue(args[ix])) != null) {
                    createDBs(db);
                } else {
                    System.out.println("Unknown type: [" + args[ix]
                            + "], ignoring.");
                }
                ix = ix + 1;
            }
        } else {
            createDBs(DB.ALL);
        }
    }

    /**
     * @param args
     */
    private void instanceMainAudit(final String[] args) {
        usage();
        System.out.println("Parameters: " + Arrays.asList(args));
        if (args.length > 3) {
            int ix = 3;
            while (ix < args.length) {
                DB db;
                if ((db = DB.forValue(args[ix])) != null) {
                    readDBs(db);
                    System.out.println("loaded " + args[ix] + " DB");
                } else {
                    System.out.println("Unknown type: [" + args[ix]
                            + "], ignoring.");
                }
                ix = ix + 1;
            }
        } else {
            readDBs(DB.ALL);
        }
        System.out.println("Retailers");
        countEntriesByCountry(retailersDB);
        System.out.println("Stores");
        countEntriesByCountry(storesDB);
        System.out.println("Curations");
        countEntriesByCountry(curationsDB);
        System.out.println("Deals");
        countEntriesByCentre(dealsDB);
        System.out.println("Events");
        countEntriesByCentre(eventsDB);
        System.out.println("Services");
        countEntriesByCentre(centreDirectoryDB);
        System.out.println("Centres");
        countEntriesByCountry(centreDB);
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        if (args.length < 1) {

            System.out
                    .println("Please provide an Api-key on the command-line.");
            System.out
                    .println("An api-key parameter is required. Optionally, an additional parameter may be supplied - 'create' to build the local DB from the remote APIs, 'audit' to load the DB from local files.");
            System.out
                    .println("Note that the 'create' parameter will result in hundreds of API calls being made, and will take close to an hour to complete.");

        } else {

            API_KEY = args[0]; // set the ApiKey
            if (args.length > 2) {
                // set the DB file path
                FILE_PATH = args[2];
            }

            final Main main = new Main();

            if (args.length > 1) {
                if ("create".equals(args[1].toLowerCase())) {
                    // create the local JSON files by loading all entities from
                    // the APIs
                    main.instanceMainCreate(args);
                } else if ("audit".equals(args[1].toLowerCase())) {
                    // load the (existing) local JSON files and print them out
                    // to the console
                    main.instanceMainAudit(args);
                } else {
                    System.out.println("Invalid command-line argument - '"
                            + args[1]
                            + "' - must be either 'create' or 'audit'.");
                    System.out
                            .println("Note that the 'create' parameter will result in hundreds of API calls being made, and will take close to an hour to complete.");
                }
            }
        }
    }

}
