package util;

import exceptions.FitManagerException;

public interface UserInputParser<T> {
    public T parse(String value) throws FitManagerException;
}
