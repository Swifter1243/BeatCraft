package com.beatcraft.lightshow.environment.weave;

import com.beatcraft.lightshow.environment.Environment;
import com.beatcraft.lightshow.environment.EnvironmentV3;

public class WeaveEnvironment extends EnvironmentV3 {
    /*
     * Environment notes:
     * 4 major groups
     * each with 4 subgroups
     *
     *
     *
     *
     */

    @Override
    public String getID() {
        return "WeaveEnvironment";
    }

    @Override
    public void setup() {

    }

    @Override
    public WeaveEnvironment reset() {
        return this;
    }
}
