package mindustry.world.blocks;

import arc.func.*;
import arc.graphics.g2d.TextureRegion;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;

import static mindustry.Vars.*;

public class TextSelection {
    /*
     * private static TextField search;
     * private static int rowCount;
     * 
     * public static void buildTable(Table table, Seq<TextureRegion> items, Prov<T>
     * holder, Cons<T> consumer) {
     * buildTable(table, items, holder, consumer, true);
     * }
     * 
     * public static void buildTable(Table table, Seq<TextureRegion> items, Prov<T>
     * holder, Cons<T> consumer,
     * boolean closeSelect) {
     * buildTable(null, table, items, holder, consumer, closeSelect, 5, 4);
     * }
     * 
     * public static void buildTable(Table table, Seq<TextureRegion> items, Prov<T>
     * holder, Cons<T> consumer,
     * int columns) {
     * buildTable(null, table, items, holder, consumer, true, 5, columns);
     * }
     * 
     * public static void buildTable(Block block, Table table, Seq<TextureRegion>
     * items, Prov<T> holder,
     * Cons<T> consumer) {
     * buildTable(block, table, items, holder, consumer, true, 5, 4);
     * }
     * 
     * public static void buildTable(Block block, Table table, Seq<TextureRegion>
     * items, Prov<T> holder, Cons<T> consumer,
     * boolean closeSelect) {
     * buildTable(block, table, items, holder, consumer, closeSelect, 5, 4);
     * }
     * 
     * public static void buildTable(Block block, Table table, Seq<TextureRegion>
     * items, Prov<T> holder, Cons<T> consumer,
     * int rows, int columns) {
     * buildTable(block, table, items, holder, consumer, true, rows, columns);
     * }
     * 
     * public static void buildTable(Table table, Seq<TextureRegion> items, Prov<T>
     * holder, Cons<T> consumer, int rows,
     * int columns) {
     * buildTable(null, table, items, holder, consumer, true, rows, columns);
     * }
     * 
     * public static void buildTable(@Nullable Block block, Table table,
     * Seq<TextureRegion> items,
     * Prov<TextureRegion> holder, Cons<TextureRegion> consumer, boolean
     * closeSelect, int rows, int columns) {
     * ButtonGroup<ImageButton> group = new ButtonGroup<>();
     * group.setMinCheckCount(0);
     * Table cont = new Table().top();
     * cont.defaults().size(40);
     * 
     * if (search != null)
     * search.clearText();
     * 
     * Runnable rebuild = () -> {
     * group.clear();
     * cont.clearChildren();
     * 
     * var text = search != null ? search.getText() : "";
     * int i = 0;
     * rowCount = 0;
     * 
     * Seq<TextureRegion> list = items
     * .select(u -> (text.isEmpty() ||
     * u.localizedName.toLowerCase().contains(text.toLowerCase())));
     * for (TextureRegion item : list) {
     * if (!item.unlockedNow()
     * || (item instanceof Item checkVisible &&
     * state.rules.hiddenBuildItems.contains(checkVisible))
     * || item.isHidden())
     * continue;
     * 
     * ImageButton button = cont
     * .button(Tex.whiteui, Styles.clearNoneTogglei, Mathf.clamp(item.selectionSize,
     * 0f, 40f), () -> {
     * if (closeSelect)
     * control.input.config.hideConfig();
     * }).tooltip(item.localizedName).group(group).get();
     * button.changed(() -> consumer.get(button.isChecked() ? item : null));
     * button.getStyle().imageUp = new TextureRegionDrawable(item.uiIcon);
     * button.update(() -> button.setChecked(holder.get() == item));
     * 
     * if (i++ % columns == (columns - 1)) {
     * cont.row();
     * rowCount++;
     * }
     * }
     * };
     * 
     * rebuild.run();
     * 
     * Table main = new Table().background(Styles.black6);
     * if (rowCount > rows * 1.5f) {
     * main.table(s -> {
     * s.image(Icon.zoom).padLeft(4f);
     * search = s.field(null, text ->
     * rebuild.run()).padBottom(4).left().growX().get();
     * search.setMessageText("@players.search");
     * }).fillX().row();
     * }
     * 
     * ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
     * pane.setScrollingDisabled(true, false);
     * 
     * if (block != null) {
     * pane.setScrollYForce(block.selectScroll);
     * pane.update(() -> {
     * block.selectScroll = pane.getScrollY();
     * });
     * }
     * 
     * pane.setOverscroll(false, false);
     * main.add(pane).maxHeight(40 * rows);
     * table.top().add(main);
     * }
     */}