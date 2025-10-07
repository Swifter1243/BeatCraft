package com.beatcraft.client.screen;

import com.beatcraft.common.utils.MathUtil;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class OptionList {

    public static class Option {

        protected final Component displayName;

        public Option(Component displayName) {
            this.displayName = displayName;
        }

    }

    public static class SliderOption extends Option {

        private final float minValue;
        private final float maxValue;
        private final int steps;
        private final float step;
        private final Callable<Float> getValue;
        private final Consumer<Float> setValue;

        public SliderOption(Component displayName, float minVal, float maxVal, int steps, Callable<Float> getValue, Consumer<Float> setValue) {
            super(displayName);
            minValue = minVal;
            maxValue = maxVal;
            this.steps = steps;
            this.setValue = setValue;
            this.getValue = getValue;
            this.step = (maxVal - minVal) / (float) steps;
        }
    }

    public static class MultiChoiceOption<T> extends Option {

        private final Callable<T> getValue;
        private final Consumer<T> setValue;
        private final LinkedHashMap<T, Component> valueDisplays;

        public MultiChoiceOption(Component displayName, LinkedHashMap<T, Component> valueDisplays, Callable<T> getValue, Consumer<T> setValue) {
            super(displayName);
            this.getValue = getValue;
            this.setValue = setValue;
            this.valueDisplays = valueDisplays;
        }


    }


    private static final int OPTS_PER_PAGE = 8;
    private int opts = 0;
    public int page = 0;
    public int pageCount;
    public final ArrayList<ArrayList<Option>> pages = new ArrayList<>();
    private boolean built = false;
    private boolean multiplePages = false;

    public OptionList() {
        pages.add(new ArrayList<>());
    }

    public OptionList addOption(Option option) {
        if (built) throw new RuntimeException("Cannot add pages after being built");
        if (opts == OPTS_PER_PAGE) {
            page++;
            opts = 1;
            pages.add(new ArrayList<>());
        } else {
            opts++;
        }
        pages.getLast().add(option);
        return this;
    }

    public OptionList build() {
        built = true;
        pageCount = page+1;
        multiplePages = pageCount > 1;
        page = 0;
        return this;
    }


}
