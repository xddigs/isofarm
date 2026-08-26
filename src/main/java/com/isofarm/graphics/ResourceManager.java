package com.isofarm.graphics;

import com.isofarm.data.*;
import com.isofarm.item.*;
import com.isofarm.utils.K;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ResourceManager {
    private static final Logger log = LoggerFactory.getLogger(ResourceManager.class);
    private final Shader defaultShader;
    private final Shader outlineShader;
    private final Shader rainShader;
    private final Shader motionBlurShader;
    private final Shader shadowMapShader;
    private final Shader blurShader;

    private final Mesh screenQuadMesh;
    private final Mesh blockMesh;
    private final Mesh selectionMesh;
    private final Mesh spriteMesh;
    private final Mesh flowerMesh;
    private final Mesh playerMesh;
    private final Mesh destroyOverlayMesh;

    private final TextureAtlas blocksAtlas;

    private final SpriteSheet wheat;
    private final SpriteSheet carrot;
    private final SpriteSheet potato;
    private final SpriteSheet beetroot;
    private static SpriteSheet seedIcons;
    private static SpriteSheet cropIcons;
    private static SpriteSheet toolIcons;
    private static SpriteSheet blockIcons;
    private static SpriteSheet materialIcons;
    private final SpriteSheet inventoryIcons;

    private final SpriteSheet playerSpriteSheet;
    private final SpriteSheet destroyTexture;
    private static Map<CropType, SpriteSheet> cropSpritesheets;

    public ResourceManager() {
        this.defaultShader = new Shader(K.Paths.DEFAULT_VERT_SHADER, K.Paths.DEFAULT_FRAG_SHADER);
        this.outlineShader = new Shader(K.Paths.OUTLINE_VERT_SHADER, K.Paths.OUTLINE_FRAG_SHADER);
        this.rainShader = new Shader(K.Paths.RAIN_VERT_SHADER, K.Paths.RAIN_FRAG_SHADER);
        this.motionBlurShader = new Shader(K.Paths.MOTION_BLUR_VERT_SHADER, K.Paths.MOTION_BLUR_FRAG_SHADER);
        this.shadowMapShader = new Shader(K.Paths.SHADOW_VERT_SHADER, K.Paths.SHADOW_FRAG_SHADER);
        this.blurShader = new Shader(K.Paths.BLUR_VERT_SHADER, K.Paths.BLUR_FRAG_SHADER);

        this.screenQuadMesh = Mesh.screenQuad();
        this.blockMesh = Mesh.createMesh(K.World.DEFAULT_BLOCK_DEPTH);
        this.selectionMesh = Mesh.selection();
        this.spriteMesh = Mesh.createCrop();
        this.flowerMesh = Mesh.createCrossMesh();
        this.playerMesh = Mesh.quadVertical();
        this.destroyOverlayMesh = Mesh.createDestroyOverlayMesh();

        List<String> allPaths = BlockData.getAllTexturePaths();
        this.blocksAtlas = new TextureAtlas(allPaths, 16, 16);
        for (BlockData block : BlockData.values()) {
            block.initRegions(this.blocksAtlas);
        }

        this.wheat = new SpriteSheet(K.Paths.WHEAT_TEXTURE, K.Render.CROP_TOTAL_FRAMES, 1);
        this.carrot = new SpriteSheet(K.Paths.CARROT_TEXTURE, K.Render.CROP_TOTAL_FRAMES, 1);
        this.potato = new SpriteSheet(K.Paths.POTATO_TEXTURE, K.Render.CROP_TOTAL_FRAMES, 1);
        this.beetroot = new SpriteSheet(K.Paths.BEETROOT_TEXTURE, K.Render.CROP_TOTAL_FRAMES, 1);

        blockIcons = new SpriteSheet(K.Paths.BLOCK_ICONS, K.UI.ICON_BLOCK_COLS, 2);
        toolIcons = new SpriteSheet(K.Paths.TOOL_ICONS, K.UI.ICON_TOOL_COLS, K.UI.ICON_TOOL_ROWS);
        cropIcons = new SpriteSheet(K.Paths.CROP_ICONS, K.UI.ICON_SEED_CROPS_COLS, 1);
        seedIcons = new SpriteSheet(K.Paths.SEED_ICONS, K.UI.ICON_SEED_CROPS_COLS, 1);
        materialIcons = new SpriteSheet(K.Paths.MATERIAL_ICONS, K.UI.ICON_MATERIAL_COLS, K.UI.ICON_MATERIAL_ROWS);
        inventoryIcons = new SpriteSheet(K.Paths.INVENTORY_ICONS, K.UI.ICON_INV_COLS, 1);

        this.playerSpriteSheet = new SpriteSheet(K.Paths.PLAYER_SPRITESHEET,
                Direction.values().length, 6);

        this.destroyTexture = new SpriteSheet(K.Paths.DESTROY_STAGES, K.UI.DESTROY_FRAMES, 1);

        cropSpritesheets = new EnumMap<>(CropType.class);
        cropSpritesheets.put(CropType.WHEAT, wheat);
        cropSpritesheets.put(CropType.CARROT, carrot);
        cropSpritesheets.put(CropType.POTATO, potato);
        cropSpritesheets.put(CropType.BEETROOT, beetroot);
    }

    public void dispose() {
        blockMesh.dispose();
        flowerMesh.dispose();
        selectionMesh.dispose();
        spriteMesh.dispose();
        screenQuadMesh.dispose();
        playerMesh.dispose();
        destroyOverlayMesh.dispose();

        blocksAtlas.dispose();

        wheat.dispose();
        carrot.dispose();
        potato.dispose();
        beetroot.dispose();

        cropIcons.dispose();
        seedIcons.dispose();
        toolIcons.dispose();
        blockIcons.dispose();
        materialIcons.dispose();
        inventoryIcons.dispose();

        playerSpriteSheet.dispose();

        defaultShader.dispose();
        outlineShader.dispose();
        motionBlurShader.dispose();
        rainShader.dispose();
        shadowMapShader.dispose();
        blurShader.dispose();
    }

    public Shader getDefaultShader() { return defaultShader; }
    public Shader getOutlineShader() { return outlineShader; }
    public Shader getRainShader() { return rainShader; }
    public Shader getMotionBlurShader() { return motionBlurShader; }
    public Shader getShadowMapShader() { return shadowMapShader; }
    public Shader getBlurShader() { return blurShader; }
    public Mesh getScreenQuadMesh() { return screenQuadMesh; }
    public Mesh getBlockMesh() { return blockMesh; }
    public Mesh getFlowerMesh() { return flowerMesh; }
    public Mesh getDestroyOverlayMesh() { return destroyOverlayMesh; }
    public Mesh getSelectionMesh() { return selectionMesh; }
    public Mesh getSpriteMesh() { return spriteMesh; }
    public Mesh getPlayerMesh() { return playerMesh; }
    public TextureAtlas getBlocksAtlas() { return blocksAtlas; }
    public SpriteSheet getDestroyTexture() { return destroyTexture; }
    public SpriteSheet getSeedIcons() { return seedIcons; }
    public SpriteSheet getCropIcons() { return cropIcons; }
    public SpriteSheet getToolIcons() { return toolIcons; }
    public SpriteSheet getBlockIcons() { return blockIcons; }
    public SpriteSheet getMaterialIcons() { return materialIcons; }
    public SpriteSheet getInventoryIcons() { return inventoryIcons; }
    public SpriteSheet getPlayerSpriteSheet() { return playerSpriteSheet; }
    public Map<CropType, SpriteSheet> getCropSpritesheets() { return cropSpritesheets; }

    public static SpriteSheet getItemSpriteSheet(Item item) {
        return switch (item) {
            case Crop crop -> cropSpritesheets.get(crop.getCropType());
            case Produce ignored -> cropIcons;
            case Seed ignored -> seedIcons;
            case Tool ignored -> toolIcons;
            case Material ignored -> materialIcons;
            case Block ignored -> blockIcons;
            case null, default -> null;
        };
    }

    private static int getMaterialFrame(Material material) {
        MaterialID materialID = material.getMaterialID();
        if (materialID == MaterialID.STICK) {
            return 0;
        }

        int baseCol = switch (material.getTier()) {
            case COPPER -> 0;
            case IRON -> 2;
            case STEEL -> 4;
            case GOLD -> 6;
            case PLATINUM -> 8;
            case DIAMOND -> 10;
            default -> 1;
        };

        int column = materialID == MaterialID.INGOT
                ? baseCol + 1
                : baseCol;
        return materialID.getRow() * K.UI.ICON_MATERIAL_COLS + column;
    }

    public static int getItemFrame(Item item) {
        if (item instanceof Block block && block.getType() != null) {
            return block.getType().getId() - 1;
        }

        if (item instanceof Produce produce && produce.getType() != null) {
            return produce.getType().getId();
        }

        if (item instanceof Seed seed && seed.getType() != null) {
            return seed.getType().getId();
        }

        if (item instanceof Crop crop && crop.getCropType() != null) {
            return crop.getCropType().getId();
        }

        if (item instanceof Tool tool) {
            return tool.getId() - 1;
        }

        if (item instanceof Bucket bucket) {
            return bucket.getId() - 1;
        }

        if (item instanceof Material material) {
            int col = getMaterialFrame(material);
            int row = material.getMaterialID().getRow();
            return row * K.UI.ICON_MATERIAL_COLS + col;
        }

        return 0;
    }

    public Shader getShader(String name) {
        if (name == null)  return defaultShader;
        return switch (name.toLowerCase()) {
            case "outline" -> outlineShader;
            case "rain" -> rainShader;
            case "motion_blur" -> motionBlurShader;
            case "blur" -> blurShader;
            case "shadow" -> shadowMapShader;
            case "default", "item" -> defaultShader;
            default -> {
                log.warn("Shader '{}' not found, using defaultShader", name);
                yield defaultShader;
            }
        };
    }
}
