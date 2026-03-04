package dev.patric.commonlib.api.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Fluent builder entry for GUI definitions.
 */
public final class GuiDsl {

    private GuiDsl() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Starts a chest GUI builder.
     *
     * @param key definition key.
     * @param rows chest rows (1..6).
     * @return builder.
     */
    public static Builder chest(String key, int rows) {
        return new Builder(key, GuiLayout.chestRows(rows));
    }

    /**
     * Fluent definition builder.
     */
    public static final class Builder {

        private final String key;
        private final GuiLayout layout;
        private GuiTitle title;
        private GuiBehaviorPolicy behaviorPolicy;
        private final Map<Integer, SlotDefinition> slots;

        private Builder(String key, GuiLayout layout) {
            this.key = Objects.requireNonNull(key, "key");
            this.layout = Objects.requireNonNull(layout, "layout");
            this.title = new GuiTitle(key);
            this.behaviorPolicy = GuiBehaviorPolicy.strictDefaults();
            this.slots = new LinkedHashMap<>();
        }

        /**
         * Sets GUI title.
         *
         * @param value title value.
         * @return same builder.
         */
        public Builder title(String value) {
            this.title = new GuiTitle(value);
            return this;
        }

        /**
         * Sets behavior policy.
         *
         * @param policy behavior policy.
         * @return same builder.
         */
        public Builder policy(GuiBehaviorPolicy policy) {
            this.behaviorPolicy = Objects.requireNonNull(policy, "policy");
            return this;
        }

        /**
         * Configures one slot through a fluent slot builder.
         *
         * @param slot slot index.
         * @param customizer slot customizer.
         * @return same builder.
         */
        public Builder slot(int slot, Consumer<SlotBuilder> customizer) {
            Objects.requireNonNull(customizer, "customizer");
            validateSlot(slot);
            ensureSlotFree(slot);
            SlotBuilder slotBuilder = new SlotBuilder(slot);
            customizer.accept(slotBuilder);
            slots.put(slot, slotBuilder.build());
            return this;
        }

        /**
         * Fills an inclusive slot range with one item and policy.
         *
         * @param start start slot.
         * @param end end slot.
         * @param item item view.
         * @param policy slot policy.
         * @return same builder.
         */
        public Builder fill(int start, int end, GuiItemView item, SlotInteractionPolicy policy) {
            if (start > end) {
                throw new IllegalArgumentException("start must be <= end");
            }
            for (int i = start; i <= end; i++) {
                validateSlot(i);
                ensureSlotFree(i);
                slots.put(i, new SlotDefinition(i, item, policy, List.of()));
            }
            return this;
        }

        /**
         * Applies one slot customizer to every slot in an inclusive range.
         *
         * @param start start slot.
         * @param end end slot.
         * @param customizer slot customizer.
         * @return same builder.
         */
        public Builder slotsRange(int start, int end, Consumer<SlotBuilder> customizer) {
            if (start > end) {
                throw new IllegalArgumentException("start must be <= end");
            }
            Objects.requireNonNull(customizer, "customizer");
            for (int i = start; i <= end; i++) {
                slot(i, customizer);
            }
            return this;
        }

        /**
         * Convenience helper for button slots.
         *
         * @param slot slot index.
         * @param item item view.
         * @param actions actions executed on click.
         * @return same builder.
         */
        public Builder button(int slot, GuiItemView item, List<GuiAction> actions) {
            Objects.requireNonNull(actions, "actions");
            return slot(slot, builder -> builder
                    .item(item)
                    .interaction(SlotInteractionPolicy.BUTTON_ONLY)
                    .actions(actions));
        }

        /**
         * Convenience helper for INPUT_DIALOG slots.
         *
         * @param slot slot index.
         * @param item item view.
         * @param dialogTemplateKey dialog template key.
         * @return same builder.
         */
        public Builder inputDialogSlot(int slot, GuiItemView item, String dialogTemplateKey) {
            return slot(slot, builder -> builder
                    .item(item)
                    .inputDialogSlot(dialogTemplateKey));
        }

        /**
         * Convenience helper for INPUT_DIALOG slots with explicit response bindings.
         *
         * @param slot slot index.
         * @param item item view.
         * @param dialogTemplateKey dialog template key.
         * @param bindings response->state bindings.
         * @return same builder.
         */
        public Builder dialogInputSlot(
                int slot,
                GuiItemView item,
                String dialogTemplateKey,
                List<DialogResponseBinding> bindings
        ) {
            Objects.requireNonNull(bindings, "bindings");
            return slot(slot, builder -> builder
                    .item(item)
                    .interaction(SlotInteractionPolicy.INPUT_DIALOG)
                    .action(new OpenDialogAction(
                            dialogTemplateKey,
                            new DialogOpenOptionsMapping(true, true, Map.of(), bindings)
                    )));
        }

        /**
         * Convenience helper for transfer slots.
         *
         * @param slot slot index.
         * @param item item view.
         * @param policy transfer policy.
         * @return same builder.
         */
        public Builder transferSlot(int slot, GuiItemView item, SlotInteractionPolicy policy) {
            if (policy != SlotInteractionPolicy.TAKE_ONLY
                    && policy != SlotInteractionPolicy.DEPOSIT_ONLY
                    && policy != SlotInteractionPolicy.TAKE_DEPOSIT) {
                throw new IllegalArgumentException("policy must be a transfer policy");
            }
            return slot(slot, builder -> builder
                    .item(item)
                    .transferSlot(policy));
        }

        /**
         * Convenience helper for a stateful switch slot.
         *
         * @param slot slot index.
         * @param stateKey state key.
         * @param onItem item to render for enabled state.
         * @param offItem item to render for disabled state.
         * @param defaultOn whether initial visual state is enabled.
         * @return same builder.
         */
        public Builder switchSlot(
                int slot,
                String stateKey,
                GuiItemView onItem,
                GuiItemView offItem,
                boolean defaultOn
        ) {
            GuiItemView initial = defaultOn ? onItem : offItem;
            return slot(slot, builder -> builder
                    .item(initial)
                    .interaction(SlotInteractionPolicy.BUTTON_ONLY)
                    .action(new ToggleStateAction(stateKey, "true", "false", onItem, offItem, true)));
        }

        /**
         * Convenience helper for submenu navigation slots.
         *
         * @param slot slot index.
         * @param item item view.
         * @param targetMenuKey target menu key.
         * @return same builder.
         */
        public Builder subMenuSlot(int slot, GuiItemView item, String targetMenuKey) {
            return slot(slot, builder -> builder
                    .item(item)
                    .interaction(SlotInteractionPolicy.BUTTON_ONLY)
                    .action(new OpenSubMenuAction(targetMenuKey, true, true)));
        }

        /**
         * Convenience helper for one back-navigation slot.
         *
         * @param slot slot index.
         * @param item item view.
         * @return same builder.
         */
        public Builder backSlot(int slot, GuiItemView item) {
            return slot(slot, builder -> builder
                    .item(item)
                    .interaction(SlotInteractionPolicy.BUTTON_ONLY)
                    .action(new BackMenuAction()));
        }

        /**
         * Builds immutable definition.
         *
         * @return immutable definition.
         */
        public GuiDefinition build() {
            return new GuiDefinition(key, layout, title, slots, behaviorPolicy);
        }

        private void validateSlot(int slot) {
            if (slot < 0 || slot >= layout.size()) {
                throw new IllegalArgumentException("slot out of layout bounds: " + slot + " for size " + layout.size());
            }
        }

        private void ensureSlotFree(int slot) {
            if (slots.containsKey(slot)) {
                throw new IllegalArgumentException("slot already configured: " + slot);
            }
        }
    }

    /**
     * Fluent slot builder.
     */
    public static final class SlotBuilder {

        private final int slot;
        private GuiItemView item;
        private SlotInteractionPolicy interaction;
        private final List<GuiAction> actions;

        private SlotBuilder(int slot) {
            this.slot = slot;
            this.item = null;
            this.interaction = SlotInteractionPolicy.BUTTON_ONLY;
            this.actions = new ArrayList<>();
        }

        /**
         * Sets visual item payload.
         *
         * @param itemView item view.
         * @return same builder.
         */
        public SlotBuilder item(GuiItemView itemView) {
            this.item = itemView;
            return this;
        }

        /**
         * Sets slot interaction policy.
         *
         * @param policy policy.
         * @return same builder.
         */
        public SlotBuilder interaction(SlotInteractionPolicy policy) {
            this.interaction = Objects.requireNonNull(policy, "policy");
            return this;
        }

        /**
         * Adds one action.
         *
         * @param action action.
         * @return same builder.
         */
        public SlotBuilder action(GuiAction action) {
            this.actions.add(Objects.requireNonNull(action, "action"));
            return this;
        }

        /**
         * Adds a list of actions.
         *
         * @param values actions.
         * @return same builder.
         */
        public SlotBuilder actions(List<GuiAction> values) {
            Objects.requireNonNull(values, "values");
            values.forEach(this::action);
            return this;
        }

        /**
         * Shortcut for a button slot running a command.
         *
         * @param commandTemplate command template.
         * @return same builder.
         */
        public SlotBuilder button(String commandTemplate) {
            this.interaction = SlotInteractionPolicy.BUTTON_ONLY;
            this.actions.add(new RunCommandAction(commandTemplate, false));
            return this;
        }

        /**
         * Shortcut for opening a dialog from a slot.
         *
         * @param dialogTemplateKey dialog template key.
         * @return same builder.
         */
        public SlotBuilder inputDialogSlot(String dialogTemplateKey) {
            this.interaction = SlotInteractionPolicy.INPUT_DIALOG;
            this.actions.add(new OpenDialogAction(dialogTemplateKey, DialogOpenOptionsMapping.defaults()));
            return this;
        }

        /**
         * Shortcut for transfer slots.
         *
         * @param policy transfer policy.
         * @return same builder.
         */
        public SlotBuilder transferSlot(SlotInteractionPolicy policy) {
            this.interaction = Objects.requireNonNull(policy, "policy");
            return this;
        }

        private SlotDefinition build() {
            return new SlotDefinition(slot, item, interaction, actions);
        }
    }
}
