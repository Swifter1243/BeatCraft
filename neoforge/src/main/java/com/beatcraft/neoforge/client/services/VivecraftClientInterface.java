package com.beatcraft.neoforge.client.services;

import com.beatcraft.client.services.IVivecraftClientInterface;
import org.joml.Matrix4f;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.helpers.RenderHelper;

public class VivecraftClientInterface implements IVivecraftClientInterface {
    @Override
    public boolean isVRNonNull() {
        return ClientDataHolderVR.getInstance().vr != null;
    }

    @Override
    public Matrix4f getVRModelView() {
        return RenderHelper.getVRModelView(ClientDataHolderVR.getInstance().currentPass);
    }

    @Override
    public boolean isVRActive() {
        return ClientDataHolderVR.getInstance().vr.isActive();
    }
}
