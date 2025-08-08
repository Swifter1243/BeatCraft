package com.beatcraft.fabric.client.vivecraft_services;

import com.beatcraft.client.vivecraft_services.IVivecraftClientInterface;
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
}
