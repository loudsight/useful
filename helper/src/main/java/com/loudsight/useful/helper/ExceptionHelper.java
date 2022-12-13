package com.loudsight.useful.helper;

public class ExceptionHelper {

//    fun <T, R> wrap(checkedFunction: (T) -> R): (T) -> R {
//        return { t ->
//            try {
//                checkedFunction.invoke(t)
//            } catch (e1: Exception) {
//                throw e1
//            }
//        }
//    }
//
//    public static <R> wrap(FunctionX<?, R, ?> checkedFunction) {
//        return {
//            try {
//                checkedFunction.apply();
//            } catch (Exception e1) {
//                throw e1;
//            }
//        }
//    }

    public interface Function0<R, E extends Exception> {
        R apply() throws E;
    }

    public static <T, R, E extends Exception> R wrap(Function0<R, E> checkedFunction) {
        try {
            return checkedFunction.apply();
        } catch (Exception e1) {
            uncheckedThrow(e1);
        }
        throw new RuntimeException("What!!! This should not be possible");
    }

    public static <T extends Throwable> RuntimeException uncheckedThrow(Throwable e) throws T {
        throw (T) e;
    }
}