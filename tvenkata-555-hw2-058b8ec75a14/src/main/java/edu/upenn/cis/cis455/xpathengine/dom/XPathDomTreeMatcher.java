package edu.upenn.cis.cis455.xpathengine.dom;

import edu.upenn.cis.cis455.xpathengine.models.XPathFilters;
import edu.upenn.cis.cis455.xpathengine.models.XPathQuery;
import edu.upenn.cis.cis455.xpathengine.models.XPathQueryNode;

import java.util.*;

public class XPathDomTreeMatcher {

    public boolean match(DomTree domTree, XPathQuery xPathQuery) {
        Set<DomElement> frontiers = new HashSet<>();
        frontiers.add(domTree.getRootElement());
        int index = 0;
        for(XPathQueryNode xPathQueryNode : xPathQuery.getxPathQueryNodeList()) {

            if(xPathQueryNode.getRelativePosition() == -1) {
                frontiers = getNewFrontiersPlusInfinity(frontiers);
            } else {

                if(index!=0 && !xPathQueryNode.isParent() && !xPathQueryNode.isCurrent()) {
                    frontiers = getNewFrontiersPlus1(frontiers);
                }
            }

            if(xPathQueryNode.isParent()) {
                frontiers = getNewFrontiersMinus1(frontiers);
            }
            frontiers = filteredElements(frontiers, xPathQueryNode);

            index += 1;
        }
        return frontiers.size() > 0;
    }

    public Set<DomElement> filteredElements(Set<DomElement> frontiers, XPathQueryNode xPathQueryNode) {

        Set<DomElement> filteredElements = new HashSet<>();
        for (DomElement domElement : frontiers) {
            if(xPathQueryNode.isWildCard() || xPathQueryNode.isParent() || xPathQueryNode.isCurrent()
                    || xPathQueryNode.getNodeName().equals(domElement.getTagName())) {
                boolean success = true;
                for (XPathFilters xPathFilters : xPathQueryNode.getxPathFiltersList()) {
                    boolean filterCheck = false;
                    for(String textNode : domElement.getTextNodes()) {
                        if(xPathFilters.getFilterType() == XPathFilters.FilterType.EXACT_MATCH) {
                            filterCheck = filterCheck || xPathFilters.getMatchingValue().equals(textNode);
                        } else {
                            filterCheck = filterCheck || textNode.contains(xPathFilters.getMatchingValue());
                        }
                    }
                    success = success && filterCheck;
                }
                if(success) {
                    filteredElements.add(domElement);
                }
            }
        }
        return filteredElements;
    }


    public Set<DomElement> getNewFrontiersPlus1(Set<DomElement> frontiers) {
        Set<DomElement> newFrontiers = new HashSet<>();
        for(DomElement frontierElement : frontiers) {
            newFrontiers.addAll(frontierElement.getChildPointers());
        }
        return newFrontiers;
    }

    public Set<DomElement> getNewFrontiersMinus1(Set<DomElement> frontiers) {
        Set<DomElement> newFrontiers = new HashSet<>();
        for(DomElement frontierElement : frontiers) {
            newFrontiers.add(frontierElement.getParentPointer());
        }
        return newFrontiers;
    }

    public Set<DomElement> getNewFrontiersPlusInfinity(Set<DomElement> frontiers) {
        Set<DomElement> newFrontiers = new HashSet<>();
        for(DomElement frontierElement : frontiers) {
            newFrontiers.addAll(getAllNestedChildern(frontierElement));
        }
        return newFrontiers;
    }


    public Set<DomElement> getAllNestedChildern(DomElement domElement) {
        LinkedList<DomElement> currentLevel = new LinkedList<>();
        currentLevel.add(domElement);
        Set<DomElement> visited = new HashSet<>();
        while (!currentLevel.isEmpty()) {
            DomElement currentNode = currentLevel.remove();
            if(visited.contains(currentNode)) {
                continue;
            }
            visited.add(currentNode);
            currentLevel.addAll(currentNode.getChildPointers());
        }
        visited.remove(domElement);
        return visited;
    }

    public static void main(String[] args) {



    }

}
