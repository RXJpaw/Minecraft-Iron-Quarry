package pw.rxj.iron_quarry.compat.iris;

import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import pw.rxj.iron_quarry.render.RenderUtil;

public class IrisClient {
    public static void register() {
        IrisApi irisApi = IrisApi.getInstance();

        RenderUtil.getLinePipelines().forEach(pipeline -> irisApi.assignPipeline(pipeline, IrisProgram.LINES));
    }
}
