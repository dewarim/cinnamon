package cinnamon

class ObjectTypeService {

    // used in TranslationMetadataTrigger
    public Set<ObjectType> findAllByNameList(List<String> names) {
        // this is probably not the fastest possible implementation - but it's the easiest.
        Set<ObjectType> oTypes = new HashSet<ObjectType>();
        for(String name : names){
            ObjectType ot = ObjectType.findByName(name);
            if(ot != null){
                oTypes.add(ot);
            }
        }
        return oTypes;
    }
}
