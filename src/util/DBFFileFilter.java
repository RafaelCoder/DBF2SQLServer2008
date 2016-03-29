package util;

import java.io.File;
import java.io.FileFilter;

public class DBFFileFilter implements FileFilter {

    private final String[] okFileExtensions = new String[]{"DBF"};

    @Override
    public boolean accept(File pathname) {
        for (String extension : okFileExtensions) {
            if (pathname.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
