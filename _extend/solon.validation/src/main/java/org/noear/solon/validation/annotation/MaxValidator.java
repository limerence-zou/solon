package org.noear.solon.validation.annotation;

import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.util.StringUtils;
import org.noear.solon.validation.Validator;

/**
 *
 * @author noear
 * @since 1.0
 * */
public class MaxValidator implements Validator<Max> {
    public static final MaxValidator instance = new MaxValidator();

    @Override
    public String message(Max anno) {
        return anno.message();
    }

    @Override
    public Result validateOfEntity(Max anno, String name, Object val, StringBuilder tmp) {
        return Validator.super.validateOfEntity(anno, name, val, tmp);
    }

    @Override
    public Result validateOfContext(Context ctx, Max anno, String name, StringBuilder tmp) {
        String val = ctx.param(name);

        if (StringUtils.isInteger(val) == false || Long.parseLong(val) > anno.value()) {
            tmp.append(',').append(name);
        }

        if (tmp.length() > 1) {
            return Result.failure(tmp.substring(1));
        } else {
            return Result.succeed();
        }
    }
}
