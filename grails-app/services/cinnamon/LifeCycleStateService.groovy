package cinnamon

import cinnamon.lifecycle.LifeCycleState
import cinnamon.lifecycle.LifeCycle

class LifeCycleStateService {

    static transactional = false

    /**
     * When an object with a LCS is copied, the LCS of the copy often has to be different from the
     * original one. For example, a user copies an archived version of the last product leaflet to
     * use it as a template. The LCS of this copy should not be "archived" but something like
     * "authoring" or "in_work".
     * fetchCopyStates compiles a list of all LifeCycleStates of the LCS's LifeCycle.
     * If no LC is defined, it just returns the LCS itself in a list with one entry.
     * member
     * @param lcs the LCS object for which you need the lifeCycleForCopy-selection list.
     * @return a list of all LifeCycleStates connected to the same LifeCycle as the parameter LCS
     * returns a list containing the parameter itself if the parameter is valid, or an empty list otherwise.
     */
    List<LifeCycleState> fetchCopyStates(LifeCycleState lcs){
        LifeCycle lc = lcs?.lifeCycle
        if(lc){
            return lc.fetchStates().asList()
        }
        else{
            if(lcs){
                return [lcs]
            }
            else{
                return []
            }
        }
    }
}
