package modules.vgmdb;

public enum VGMDBPaths {
    // album json
    SERIES("$.products.*.names", true) {
        @Override
        public String path() {
            if(super.varience == Variences.JAPANESE) {
                return super.path() + ".ja-latn";
            }
            else if(super.varience == Variences.ENGLISH) {
                return super.path() + ".en";
            }
            return super.path();
        }
    },
    ALBUM_ART_THUMB_URL("$.picture_small", false),
    ALBUM_ART_FULL_URL("$.picture_full", false),
    ALBUM_NAME("$.name", false),
    ARTIST("$.performers.*.names", true) {
        @Override
        public String path() {
            if(super.varience == Variences.JAPANESE) {
                return super.path() + ".ja";
            }
            else if(super.varience == Variences.ENGLISH) {
                return super.path() + ".en";
            }
            return super.path();
        }
    },
    RELEAST_DATE("$.release_date", false),
    TRACKS("$.discs.[%d].tracks.*.names", true) {
        @Override
        public String path() {
            super.path = String.format(super.path, super.disc);
            if(super.varience == Variences.JAPANESE) {
                return super.path() + ".Japanese";
            }
            else if(super.varience == Variences.ENGLISH) {
                return super.path() + ".English";
            }
            else if(super.varience == Variences.ROMANJI) {
                return super.path() + ".Romaji";
            }
            return super.path();
        }
    },
    CATALOG("$.catalog", false),
    ADDITIONAL_NOTES("$.notes", false),
    
    // search json
    SEARCH_ALBUM_TITLES("$.results.albums.*.titles", true) {
        @Override
        public String path() {
            if(super.varience == Variences.JAPANESE) {
                return super.path() + ".ja";
            }
            else if(super.varience == Variences.ENGLISH) {
                return super.path() + ".en";
            }
            else if(super.varience == Variences.ROMANJI) {
                return super.path() + ".ja-latn";
            }
            return super.path();
        }
    },
    SEARCH_ALBUM_ID("$.results.albums.*.link", false),
    SEARCH_QUERY("$.query", false),
    OTHER_SITE_NAMES("$.stores.*.name", false),
    OTHER_SITE_URLS("$.stores.*.link", false),
    SITE_URL("$.vgmdb_link", false);
    
    private String path;
    private int disc;
    private boolean hasVarience;
    private Variences varience;

    private VGMDBPaths(String path, boolean hasVarience) {
        this.path = path;
        disc = 0;
        this.hasVarience = hasVarience;
        varience = Variences.ENGLISH;
    }

    public String path() {
        return path;
    }

    public VGMDBPaths varience(Variences varience) {
        this.varience = varience;
        return this;
    }

    public boolean hasVarience() {
        return hasVarience;
    }

    /**
     * Start from 0
     * 
     * @param disc
     * @return
     */
    public VGMDBPaths disc(int disc) {
        this.disc = disc;
        return this;
    }
}
