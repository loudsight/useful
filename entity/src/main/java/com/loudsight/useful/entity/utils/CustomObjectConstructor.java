package com.loudsight.useful.entity.utils;

import com.loudsight.meta.MetaRepository;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomObjectConstructor extends Constructor {

        public CustomObjectConstructor(LoaderOptions loadingConfig) {
            super(loadingConfig);
//            this.yamlConstructors.put(new Tag("!" + SimpleFollowerTradeStrategySpecification.class.getName()), new ConstructCustomObject());
        }

        private static class ConstructCustomObject extends AbstractConstruct {

            @Override
            public Object construct(Node node) {
                var mappings = convertNodeToMap(node);
                var meta = MetaRepository.getInstance().getMeta(mappings.get("__className__").toString());

                return meta.newInstance(mappings);
            }
        }

    public static Map<String, Object> convertNodeToMap(Node node) {
        if (node instanceof MappingNode mappingNode) {
            List<NodeTuple> nodeTuples = mappingNode.getValue();

            Map<String, Object> map = new HashMap<>();

            for (NodeTuple tuple : nodeTuples) {
                Node keyNode = tuple.getKeyNode();
                Node valueNode = tuple.getValueNode();

                String key = convertNodeToObject(keyNode).toString();
                Object value = convertNodeToObject(valueNode);

                map.put(key, value);
            }

            return map;
        }

        return null; // or throw an exception for unsupported node types
    }

    private static Object convertNodeToObject(Node node) {
        // Implement the conversion logic for different node types here
        // For example, you can handle ScalarNode, SequenceNode, etc.

        // Assume all non-mapping nodes are scalar values
        if (node instanceof ScalarNode scalarNode) {
            return scalarNode.getValue();
        }

        throw new IllegalArgumentException("Unexpected type ");
    }

}