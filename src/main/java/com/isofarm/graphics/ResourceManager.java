package com.isofarm.graphics;

import com.isofarm.data.*;
import com.isofarm.graphics.gltf.GLTFLoader;
import com.isofarm.graphics.gltf.GLTFModel;
import com.isofarm.item.*;
import com.isofarm.utils.K;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ResourceManager {
    public static final ResourceManager rem = new ResourceManager();
    private static final Logger log = LoggerFactory.getLogger(ResourceManager.class);

    private static final SpriteSheet seedIcons = new SpriteSheet(K.Paths.SEED_ICONS, K.UI.ICON_SEED_CROPS_COLS, 1);
    private static final SpriteSheet cropIcons = new SpriteSheet(K.Paths.CROP_ICONS, K.UI.ICON_SEED_CROPS_COLS, 1);
    private static final SpriteSheet toolIcons = new SpriteSheet(K.Paths.TOOL_ICONS, K.UI.ICON_TOOL_COLS, K.UI.ICON_TOOL_ROWS);
    private static final SpriteSheet blockIcons = new SpriteSheet(K.Paths.BLOCK_ICONS, K.UI.ICON_BLOCK_COLS, K.UI.ICON_BLOCK_ROWS);
    private static final SpriteSheet materialIcons = new SpriteSheet(K.Paths.MATERIAL_ICONS, K.UI.ICON_MATERIAL_COLS, K.UI.ICON_MATERIAL_ROWS);
    private static final SpriteSheet usablesIcons = new SpriteSheet(K.Paths.USABLES_ICONS, K.UI.ICON_USABLES_COLS, 1);
    private static final SpriteSheet inventoryIcons = new SpriteSheet(K.Paths.INVENTORY_ICONS, K.UI.ICON_INV_COLS, 1);
    private static final SpriteSheet bookAnimationSheet = new SpriteSheet(K.Paths.BOOK_ANIMATION, 16, 1);
    private static final SpriteSheet heartsSpriteSheet = new SpriteSheet(K.Paths.HEARTS_SPRITESHEET, 1, K.UI.ICON_HEARTS_ROWS);
    private static final SpriteSheet destroyTexture = new SpriteSheet(K.Paths.DESTROY_STAGES, K.UI.DESTROY_FRAMES, 1);

    private static final SpriteSheet wheat = new SpriteSheet(K.Paths.WHEAT_TEXTURE, K.Render.CROP_TOTAL_FRAMES, 1);
    private static final SpriteSheet carrot = new SpriteSheet(K.Paths.CARROT_TEXTURE, K.Render.CROP_TOTAL_FRAMES, 1);
    private static final SpriteSheet potato = new SpriteSheet(K.Paths.POTATO_TEXTURE, K.Render.CROP_TOTAL_FRAMES, 1);
    private static final SpriteSheet beetroot = new SpriteSheet(K.Paths.BEETROOT_TEXTURE, K.Render.CROP_TOTAL_FRAMES, 1);

    private static final GLTFModel playerModel = GLTFLoader.load(K.Paths.PLAYER_MODEL);

    private static final Map<CropType, SpriteSheet> cropSpritesheets = new EnumMap<>(CropType.class);

    private static final Shader defaultShader = new Shader(K.Paths.DEFAULT_VERT_SHADER, K.Paths.DEFAULT_FRAG_SHADER);
    private static final Shader rainShader = new Shader(K.Paths.RAIN_VERT_SHADER, K.Paths.RAIN_FRAG_SHADER);
    private static final Shader motionBlurShader = new Shader(K.Paths.MOTION_BLUR_VERT_SHADER, K.Paths.MOTION_BLUR_FRAG_SHADER);
    private static final Shader shadowMapShader = new Shader(K.Paths.SHADOW_VERT_SHADER, K.Paths.SHADOW_FRAG_SHADER);
    private static final Shader blurShader = new Shader(K.Paths.BLUR_VERT_SHADER, K.Paths.BLUR_FRAG_SHADER);

    private static final Mesh screenQuadMesh = Mesh.screenQuad();
    private static final Mesh blockMesh = Mesh.createMesh(K.World.DEFAULT_BLOCK_DEPTH);
    private static final Mesh selectionMesh = Mesh.selection();
    private static final Mesh spriteMesh = Mesh.createCrop();
    private static final Mesh flowerMesh = Mesh.createCrossMesh();
    private static final Mesh playerMesh = Mesh.quadVertical();
    private static final Mesh destroyOverlayMesh = Mesh.createDestroyOverlayMesh();
    private static final Texture backgroundGUI = new Texture(K.Paths.DEFAULT_BACKGROUND_GUI);
    private static final TextureAtlas blocksAtlas;

    static {
        List<String> allPaths = BlockData.getAllTexturePaths();
        blocksAtlas = new TextureAtlas(allPaths, 16, 16);

        for (BlockData block : BlockData.values()) {
            block.initRegions(blocksAtlas);
        }

        cropSpritesheets.put(CropType.WHEAT, wheat);
        cropSpritesheets.put(CropType.CARROT, carrot);
        cropSpritesheets.put(CropType.POTATO, potato);
        cropSpritesheets.put(CropType.BEETROOT, beetroot);
    }

    private ResourceManager() {
    }

    public static SpriteSheet getItemSpriteSheet(Item item) {
        return switch (item) {
            case Crop crop -> cropSpritesheets.get(crop.getCropType());
            case Produce ignored -> cropIcons;
            case Seed ignored -> seedIcons;
            case Tool ignored -> toolIcons;
            case Material ignored -> materialIcons;
            case Usable ignored -> usablesIcons;
            case Block ignored -> blockIcons;
            case null, default -> null;
        };
    }

    private static int getMaterialFrame(Material material) {
        MaterialID materialID = material.getMaterialID();
        int row = materialID.getRow();

        if (row == 1) {
            int col = switch (materialID) {
                case PAPER -> 1;
                case LEATHER -> 2;
                default -> 0;
            };
            return (row * K.UI.ICON_MATERIAL_COLS) + col;
        }

        if (material.getTier() != null) {
            int baseCol = switch (material.getTier()) {
                case IRON -> 2;
                case PLATINUM -> 4;
                case GOLDEN -> 6;
                case STEEL -> 8;
                case DIAMOND -> 10;
                default -> 0;
            };

            int column = (materialID == MaterialID.INGOT) ? baseCol + 1 : baseCol;
            return (row * K.UI.ICON_MATERIAL_COLS) + column;
        }

        return 0;
    }

    public static int getItemFrame(Item item) {
        if (item instanceof Block block && block.getType() != null) {
            int col = block.getType().getCol() - 1;
            int row = block.getType().getRow();
            return (row * K.UI.ICON_BLOCK_COLS) + col;
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
            return (tool.getRow() * K.UI.ICON_TOOL_COLS) + tool.getCol();
        }

        if (item instanceof Usable usable) {
            int col = usable.getUsablesID().getCol();
            int row = usable.getUsablesID().getRow();
            int bucketOffset = (usable instanceof Bucket bucket && bucket.isFull()) ? 1 : 0;
            return (row * K.UI.ICON_USABLES_COLS) + col + bucketOffset;
        }

        if (item instanceof Material material) {
            return getMaterialFrame(material);
        }

        return 0;
    }

    public void dispose() {
        blockMesh.dispose();
        flowerMesh.dispose();
        selectionMesh.dispose();
        spriteMesh.dispose();
        screenQuadMesh.dispose();
        playerMesh.dispose();
        destroyOverlayMesh.dispose();

        backgroundGUI.dispose();
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
        usablesIcons.dispose();
        inventoryIcons.dispose();

        playerModel.dispose();
        bookAnimationSheet.dispose();
        heartsSpriteSheet.dispose();

        defaultShader.dispose();
        motionBlurShader.dispose();
        rainShader.dispose();
        shadowMapShader.dispose();
        blurShader.dispose();
    }

    public Shader getDefaultShader() {
        return defaultShader;
    }

    public Shader getRainShader() {
        return rainShader;
    }

    public Shader getMotionBlurShader() {
        return motionBlurShader;
    }

    public Shader getShadowMapShader() {
        return shadowMapShader;
    }

    public Shader getBlurShader() {
        return blurShader;
    }

    public Mesh getScreenQuadMesh() {
        return screenQuadMesh;
    }

    public Mesh getBlockMesh() {
        return blockMesh;
    }

    public Mesh getFlowerMesh() {
        return flowerMesh;
    }

    public Mesh getDestroyOverlayMesh() {
        return destroyOverlayMesh;
    }

    public Mesh getSelectionMesh() {
        return selectionMesh;
    }

    public Mesh getSpriteMesh() {
        return spriteMesh;
    }

    public Mesh getPlayerMesh() {
        return playerMesh;
    }

    public Texture getBackgroundGUI() {
        return backgroundGUI;
    }

    public TextureAtlas getBlocksAtlas() {
        return blocksAtlas;
    }

    public SpriteSheet getDestroyTexture() {
        return destroyTexture;
    }

    public GLTFModel getPlayerModel() {
        return playerModel;
    }

    public SpriteSheet getSeedIcons() {
        return seedIcons;
    }

    public SpriteSheet getCropIcons() {
        return cropIcons;
    }

    public SpriteSheet getToolIcons() {
        return toolIcons;
    }

    public SpriteSheet getBlockIcons() {
        return blockIcons;
    }

    public SpriteSheet getMaterialIcons() {
        return materialIcons;
    }

    public SpriteSheet getUsablesIcons() {
        return usablesIcons;
    }

    public SpriteSheet getInventoryIcons() {
        return inventoryIcons;
    }

    public SpriteSheet getBookAnimationSheet() {
        return bookAnimationSheet;
    }

    public SpriteSheet getHeartsSpriteSheet() {
        return heartsSpriteSheet;
    }

    public Map<CropType, SpriteSheet> getCropSpritesheets() {
        return cropSpritesheets;
    }

    public Shader getShader(String name) {
        if (name == null) return defaultShader;

        return switch (name.toLowerCase()) {
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
