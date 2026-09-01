package com.isofarm.graphics;

import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;
import com.isofarm.graphics.gltf.GLTFModel;
import com.isofarm.graphics.gltf.GLTFNode;

import java.util.HashMap;
import java.util.Map;

public class EquipmentController {

    public static final EquipmentController ec = new EquipmentController();

    private final Map<String, GLTFNode> equipmentNodes = new HashMap<>();
    private GLTFNode currentActiveNode = null;

    public void init(GLTFModel playerModel) {
        String[] materials = new String[Tier.values().length];
        String[] types = new String[ToolType.values().length];

        for (int i = 0; i < materials.length; i++) {
            materials[i] = Tier.values()[i].name().toLowerCase();
        }

        for (int i = 0; i < types.length; i++) {
            types[i] = ToolType.values()[i].name().toLowerCase();
        }

        for (String type : types) {
            for (String material : materials) {
                String nodeName = material + "_" + type;
                GLTFNode node = playerModel.findNode(nodeName);

                if (node != null) {
                    node.setVisible(false);
                    equipmentNodes.put(nodeName, node);
                }
            }
        }
        currentActiveNode = null;
    }

    public void equip(String material, String type) {
        if (currentActiveNode != null) {
            currentActiveNode.setVisible(false);
            currentActiveNode = null;
        }

        if (material == null || type == null) {
            return;
        }

        String nodeName = material.toLowerCase() + "_" + type.toLowerCase();
        GLTFNode node = equipmentNodes.get(nodeName);
        if (node == null) {
            return;
        }

        node.setVisible(true);
        currentActiveNode = node;
    }
}