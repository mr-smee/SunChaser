package com.example.sunchaser.app.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by smee on 03/06/15.
 */
public enum PlaceType {

    AMUSEMENT_PARK(             "amusement_park",           "Amusement park",                   PlaceTypeGroup.LEISURE),
    AQUARIUM(                   "aquarium",                 "Aquarium",                         PlaceTypeGroup.LEISURE),
    ART_GALLERY(                "art_gallery",              "Art gallery",                      PlaceTypeGroup.CULTURE),
    ATM(                        "atm",                      "ATM"),
    BAKERY(                     "bakery",                   "Bakery",                           PlaceTypeGroup.EATING),
    BANK(                       "bank",                     "Bank"),
    BAR(                        "bar",                      "Bar",                              PlaceTypeGroup.EATING, PlaceTypeGroup.NIGHTLIFE),
    BEAUTY_SALON(               "beauty_salon",             "Beauty salon"),
    BICYCLE_STORE(              "bicycle_store",            "Bicycle store"),
    BOOK_STORE(                 "book_store",               "Book store",                       PlaceTypeGroup.SHOPPING, PlaceTypeGroup.CULTURE),
    BOWLING_ALLEY(              "bowling_alley",            "Bowling alley",                    PlaceTypeGroup.LEISURE),
    BUS_STATION(                "bus_station",              "Bus station",                      PlaceTypeGroup.TRAVEL),
    CAFE(                       "cafe",                     "Cafe",                             PlaceTypeGroup.EATING),
    CAMPGROUND(                 "campground",               "Campground",                       PlaceTypeGroup.LODGING),
    CAR_DEALER(                 "car_dealer",               "Car dealer"),
    CAR_RENTAL(                 "car_rental",               "Car rental",                       PlaceTypeGroup.TRAVEL),
    CAR_REPAIR(                 "car_repair",               "Car repair"),
    CAR_WASH(                   "car_wash",                 "Car wash"),
    CASINO(                     "casino",                   "Casino",                           PlaceTypeGroup.LEISURE),
    CEMETERY(                   "cemetery",                 "Cemetery"),
    CHURCH(                     "church",                   "Church",                           PlaceTypeGroup.CULTURE),
    CITY_HALL(                  "city_hall",                "City hall"),
    CLOTHING_STORE(             "clothing_store",           "Clothing store",                   PlaceTypeGroup.SHOPPING),
    CONVENIENCE_STORE(          "convenience_store",        "Convenience store"),
    COURTHOUSE(                 "courthouse",               "Courthouse"),
    DENTIST(                    "dentist",                  "Dentist"),
    DEPARTMENT_STORE(           "department_store",         "Department store",                 PlaceTypeGroup.SHOPPING),
    DOCTOR(                     "doctor",                   "Doctor"),
    ELECTRICIAN(                "electrician",              "Electrician"),
    ELECTRONICS_STORE(          "electronics_store",        "Electronics store",                PlaceTypeGroup.SHOPPING),
    EMBASSY(                    "embassy",                  "Embassy"),
    ESTABLISHMENT(              "establishment",            "Establishment"),
    FINANCE(                    "finance",                  "Finance"),
    FIRE_STATION(               "fire_station",             "Fire station"),
    FLORIST(                    "florist",                  "Florist"),
    FOOD(                       "food",                     "Food",                             PlaceTypeGroup.EATING),
    FUNERAL_HOME(               "funeral_home",             "Funeral home"),
    FURNITURE_STORE(            "furniture_store",          "Furniture store"),
    GAS_STATION(                "gas_station",              "Gas station"),
    GENERAL_CONTRACTOR(         "general_contractor",       "General contractor"),
    GROCERY_OR_SUPERMARKET(     "grocery_or_supermarket",   "Grocery or supermarket"),
    GYM(                        "gym",                      "Gym",                              PlaceTypeGroup.LEISURE),
    HAIR_CARE(                  "hair_care",                "Hair care"),
    HARDWARE_STORE(             "hardware_store",           "Hardware store"),
    HEALTH(                     "health",                   "Health"),
    HINDU_TEMPLE(               "hindu_temple",             "Hindu temple",                     PlaceTypeGroup.CULTURE),
    HOME_GOODS_STORE(           "home_goods_store",         "Home goods store",                 PlaceTypeGroup.SHOPPING),
    HOSPITAL(                   "hospital",                 "Hospital"),
    INSURANCE_AGENCY(           "insurance_agency",         "Insurance agency"),
    JEWELRY_STORE(              "jewelry_store",            "Jewelry store",                    PlaceTypeGroup.SHOPPING),
    LAUNDRY(                    "laundry",                  "Laundry"),
    LAWYER(                     "lawyer",                   "Lawyer"),
    LIBRARY(                    "library",                  "Library",                          PlaceTypeGroup.CULTURE),
    LIQUOR_STORE(               "liquor_store",             "Liquor store"),
    LOCAL_GOVERNMENT_OFFICE(    "local_government_office",  "Local government office"),
    LOCKSMITH(                  "locksmith",                "Locksmith"),
    LODGING(                    "lodging",                  "Lodging",                          PlaceTypeGroup.LODGING),
    MEAL_DELIVERY(              "meal_delivery",            "Meal delivery",                    PlaceTypeGroup.EATING),
    MEAL_TAKEAWAY(              "meal_takeaway",            "Meal takeaway",                    PlaceTypeGroup.EATING),
    MOSQUE(                     "mosque",                   "Mosque",                           PlaceTypeGroup.CULTURE),
    MOVIE_RENTAL(               "movie_rental",             "Movie rental"),
    MOVIE_THEATRE(              "movie_theater",            "Movie theatre",                    PlaceTypeGroup.CULTURE, PlaceTypeGroup.LEISURE),
    MOVING_COMPANY(             "moving_company",           "Moving company"),
    MUSEUM(                     "museum",                   "Museum",                           PlaceTypeGroup.CULTURE),
    NIGHT_CLUB(                 "night_club",               "Night club",                       PlaceTypeGroup.NIGHTLIFE),
    PAINTER(                    "painter",                  "Painter"),
    PARK(                       "park",                     "Park",                             PlaceTypeGroup.LEISURE),
    PARKING(                    "parking",                  "Parking"),
    PET_STORE(                  "pet_store",                "Pet store"),
    PHARMACY(                   "pharmacy",                 "Pharmacy"),
    PHYSIOTHERAPIST(            "physiotherapist",          "Physiotherapist"),
    PLACE_OF_WORSHIP(           "place_of_worship",         "Place of worship",                 PlaceTypeGroup.CULTURE),
    PLUMBER(                    "plumber",                  "Plumber"),
    POLICE(                     "police",                   "Police"),
    POST_OFFICE(                "post_office",              "Post office"),
    REAL_ESTATE_AGENCY(         "real_estate_agency",       "Real estate agency"),
    RESTAURANT(                 "restaurant",               "Restaurant",                       PlaceTypeGroup.EATING),
    ROOFING_CONTRACTOR(         "roofing_contractor",       "Roofing contractor"),
    RV_PARK(                    "rv_park",                  "RV park"),
    SCHOOL(                     "school",                   "School"),
    SHOE_STORE(                 "shoe_store",               "Shoe store",                       PlaceTypeGroup.SHOPPING),
    SHOPPING_MALL(              "shopping_mall",            "Shopping mall",                    PlaceTypeGroup.SHOPPING),
    SPA(                        "spa",                      "Spa",                              PlaceTypeGroup.LEISURE),
    STADIUM(                    "stadium",                  "Stadium",                          PlaceTypeGroup.LEISURE),
    STORAGE(                    "storage",                  "Storage"),
    STORE(                      "store",                    "Store"),
    SUBWAY_STATION(             "subway_station",           "Subway station",                   PlaceTypeGroup.TRAVEL),
    SYNAGOGUE(                  "synagogue",                "Synagogue",                        PlaceTypeGroup.CULTURE),
    TAXI_STAND(                 "taxi_stand",               "Taxi stand",                       PlaceTypeGroup.TRAVEL),
    TRAIN_STATION(              "train_station",            "Train station",                    PlaceTypeGroup.TRAVEL),
    TRAVEL_AGENCY(              "travel_agency",            "Travel agency",                    PlaceTypeGroup.TRAVEL),
    UNIVERSITY(                 "university",               "University"),
    VETERINARY_CARE(            "veterinary_care",          "Veterinary care"),
    ZOO(                        "zoo",                      "Zoo",                              PlaceTypeGroup.LEISURE),
    ;

    private final String internalName;
    private final String displayName;
    private final List<PlaceTypeGroup> groups;

    private PlaceType(String internalName, String displayName, PlaceTypeGroup... groups) {
        this.internalName = internalName;
        this.displayName = displayName;
        this.groups = Arrays.asList(groups);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getInternalName() {
        return internalName;
    }

    public Set<PlaceTypeGroup> getGroups() {
        return new HashSet<PlaceTypeGroup>(groups);
    }

    public static PlaceType fromInternalName(String typeName) {
        for (PlaceType type : values()) {
            if (type.internalName.equals(typeName)) {
                return type;
            }
        }

        return null;
    }
}
