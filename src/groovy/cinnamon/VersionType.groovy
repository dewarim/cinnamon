package cinnamon

/**
 * Cinnamon currently has several version policies for OSD (ObjectSystemData) objects.
 * When operating on a list of objects (for example, all objects in a folder), you can filter the
 * list by selecting: 
 * <li> only the newest version in the main branch of each object == HEAD</li>
 * <li> the newest versions in all branches of each object (including the main / head branch) == BRANCHES</li>
 * <li> all versions of all objects == ALL, that is, do not filter at all</li>
 * <li> selected versions == SELECTED, that is, only those objects are targeted that the user has specifically selected.
 *  Note that it is not possible to delete a single selected object which still has child objects - the version tree
 *  cannot have gaps.</li>
 */
public enum VersionType {

    /**
     * Select the newest version in the main branch.
     */
    HEAD([name: 'versionType.head', ranking:3]),

    /**
     * Select the newest versions of each branch, including the main branch.
     */
    BRANCHES([name: 'versionType.branches', ranking: 4]),

    /**
     * Select all versions.
     */
    ALL([name: 'versionType.all', ranking: 2]),

    /**
     * Target only the selected versions.
     */
    SELECTED([name: 'versionType.selected', ranking: 1]);

    String name
    String ranking

    VersionType(Map map) {
        map.each {k, v ->
            this."$k" = v
        }
    }

    String getName() {
        return name
    }

}