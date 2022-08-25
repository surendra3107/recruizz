package com.bbytes.recruiz.enums;

import java.io.File;
import java.util.Locale;

public enum SupportedDocumentType { 
    txt { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "Text files (.txt)"; 
        } 
    }, 
    msg { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "Microsoft Outlook Messages (.msg)"; 
        } 
    }, 
    pdf { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "Adobe PDF Files (.pdf)"; 
        } 
    }, 
    doc { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "Microsoft Word Documents (.doc)"; 
        } 
    }, 
    docx { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "Microsoft Word Documents (.docx)"; 
        } 
    }, 
    ppt { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "Microsoft PowerPoint Documents (.ppt)"; 
        } 
    }, 
    pptx { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "Microsoft PowerPoint Documents (.pptx)"; 
        } 
    }, 
    rtf { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "Rich Text File (.rtf)"; 
        } 
    }, 
    html { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "HTML Document (.html)"; 
        } 
    }, 
    htm { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "HTML Document (.htm)"; 
        } 
    }, 
    eml { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "Microsoft Outlook Messages (.eml)"; 
        } 
    }, 
    ini { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "Configuration Files (.ini)"; 
        } 
    }, 
    epub { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "EBook (.epub)"; 
        } 
    }, 
    mobi { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "EBook (.mobi)"; 
        } 
    }, 
    odt { 
        @Override 
        public String getDisplayName(Locale aLocale) { 
            return "Open Office Document (.odt)"; 
        } 
    }; 
 
    public boolean supports(String aFilename) { 
        // Filter by extension and also make sure no temp files are indexed... 
        return aFilename.toLowerCase().endsWith("." + name()) && !aFilename.contains("~"); 
    } 
 
    public abstract String getDisplayName(Locale aLocale); 
 
    public boolean matches(File aFile) { 
        return aFile.getName().toLowerCase().endsWith("." +name()); 
    } 
}