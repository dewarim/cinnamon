package cinnamon

import cinnamon.relation.Relation
import cinnamon.relation.resolver.RelationSide

class RelationService {

    /**
     * Update relations of all OSDs of this OSD's version tree.
     * After a part of a relation has been updated, all other relations which target
     * this object's version tree have to be updated also because the change to one object may
     * require other relations to change.<br>
     * Example:<br>
     * An image, which is referenced by several relations from documents which
     * use it, is updated and increases its version number. Now all relations which
     * use the LatestHeadResolver need to update their link to this new version.
     * @param changedOsd OSD which has already been updated (or does not need an update).
     */
    void updateRelations(ObjectSystemData changedOsd){
        for(ObjectSystemData osd : ObjectSystemData.findAllByRoot(changedOsd.root)){
            for(Relation relation : Relation.findAllByLeftOSD(osd)){
                relation.leftOSD = relation.getType().findOsdVersion(relation, osd, RelationSide.LEFT);
            }
            for(Relation relation : Relation.findAllByRightOSD(osd)){
                relation.rightOSD = relation.getType().findOsdVersion(relation, osd, RelationSide.RIGHT);
            }
        }
    }

}
