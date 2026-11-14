package com.example.data.local

data class ExcelFunctionEntry(
    val englishName: String,
    val greekName: String,
    val category: String,
    val englishSyntax: String,
    val greekSyntax: String,
    val descriptionEn: String,
    val descriptionEl: String,
    val samplePrompt: String
)

object ExcelDictionaryData {
    val categories: List<String> = listOf(
        "All",
        "Logical",
        "Lookup",
        "Math & Stats",
        "Text",
        "Date & Time",
        "Dynamic Array",
        "Financial"
    )

    val entries: List<ExcelFunctionEntry> = listOf(
        ExcelFunctionEntry(
            englishName = "IF",
            greekName = "ΑΝ",
            category = "Logical",
            englishSyntax = "=IF(A2>100, \"High\", \"Low\")",
            greekSyntax = "=ΑΝ(A2>100; \"Υψηλό\"; \"Χαμηλό\")",
            descriptionEn = "Returns one value if a condition is true and another if false.",
            descriptionEl = "Επιστρέφει μία τιμή αν η συνθήκη ισχύει και άλλη αν δεν ισχύει.",
            samplePrompt = "Check if A2 is over 100 and return High or Low"
        ),
        ExcelFunctionEntry(
            englishName = "IFS",
            greekName = "ΑΝ.ΣΥΝΟΛΑ",
            category = "Logical",
            englishSyntax = "=IFS(A2>=90, \"A\", A2>=80, \"B\", TRUE, \"C\")",
            greekSyntax = "=ΑΝ.ΣΥΝΟΛΑ(A2>=90; \"A\"; A2>=80; \"B\"; ΑΛΗΘΗΣ; \"C\")",
            descriptionEn = "Evaluates multiple conditions in order without nested IFs.",
            descriptionEl = "Ελέγχει πολλές συνθήκες στη σειρά χωρίς εμφωλευμένα ΑΝ.",
            samplePrompt = "Assign letter grade A, B, C, or F based on score in A2"
        ),
        ExcelFunctionEntry(
            englishName = "IFERROR",
            greekName = "ΑΝ.ΣΦΑΛΜΑ",
            category = "Logical",
            englishSyntax = "=IFERROR(A2/B2, 0)",
            greekSyntax = "=ΑΝ.ΣΦΑΛΜΑ(A2/B2; 0)",
            descriptionEn = "Catches formula errors (#N/A, #DIV/0!) and returns a fallback value.",
            descriptionEl = "Εντοπίζει σφάλματα (#Δ/Υ, #ΔΙΑΙΡ/0!) και επιστρέφει εναλλακτική τιμή.",
            samplePrompt = "Divide A2 by B2 and show 0 if there is a division error"
        ),
        ExcelFunctionEntry(
            englishName = "AND / OR",
            greekName = "ΚΑΙ / Η",
            category = "Logical",
            englishSyntax = "=IF(AND(A2>0, B2=\"Paid\"), \"OK\", \"Check\")",
            greekSyntax = "=ΑΝ(ΚΑΙ(A2>0; B2=\"Πληρωμένο\"); \"ΟΚ\"; \"Έλεγχος\")",
            descriptionEn = "Combines multiple logical tests (all true with AND, any true with OR).",
            descriptionEl = "Συνδυάζει πολλούς λογικούς ελέγχους (όλοι αληθείς με ΚΑΙ, έστω ένας με Η).",
            samplePrompt = "Return OK if A2 is positive and B2 equals Paid"
        ),
        ExcelFunctionEntry(
            englishName = "SUM",
            greekName = "ΑΘΡΟΙΣΜΑ",
            category = "Math & Stats",
            englishSyntax = "=SUM(B2:B50)",
            greekSyntax = "=ΑΘΡΟΙΣΜΑ(B2:B50)",
            descriptionEn = "Adds all numeric values in a cell range.",
            descriptionEl = "Προσθέτει όλους τους αριθμούς σε μια περιοχή κελιών.",
            samplePrompt = "Sum all numbers in column B from row 2 to 50"
        ),
        ExcelFunctionEntry(
            englishName = "SUMIF",
            greekName = "ΑΘΡΟΙΣΜΑ.ΑΝ",
            category = "Math & Stats",
            englishSyntax = "=SUMIF(A2:A50, \"Athens\", C2:C50)",
            greekSyntax = "=ΑΘΡΟΙΣΜΑ.ΑΝ(A2:A50; \"Αθήνα\"; C2:C50)",
            descriptionEn = "Sums cells that match a single criterion.",
            descriptionEl = "Αθροίζει κελιά που πληρούν ένα κριτήριο.",
            samplePrompt = "Αθροισε τη στήλη C όπου η στήλη A είναι Αθήνα"
        ),
        ExcelFunctionEntry(
            englishName = "SUMIFS",
            greekName = "ΑΘΡΟΙΣΜΑ.ΑΝ.ΣΥΝΟΛΑ",
            category = "Math & Stats",
            englishSyntax = "=SUMIFS(D2:D100, A2:A100, \"Sales\", B2:B100, \">100\")",
            greekSyntax = "=ΑΘΡΟΙΣΜΑ.ΑΝ.ΣΥΝΟΛΑ(D2:D100; A2:A100; \"Πωλήσεις\"; B2:B100; \">100\")",
            descriptionEn = "Sums cells that meet multiple criteria simultaneously.",
            descriptionEl = "Αθροίζει κελιά που πληρούν ταυτόχρονα πολλά κριτήρια.",
            samplePrompt = "Sum column D where column A is Sales and column B is greater than 100"
        ),
        ExcelFunctionEntry(
            englishName = "AVERAGE",
            greekName = "ΜΕΣΟΣ.ΟΡΟΣ",
            category = "Math & Stats",
            englishSyntax = "=AVERAGE(B2:B50)",
            greekSyntax = "=ΜΕΣΟΣ.ΟΡΟΣ(B2:B50)",
            descriptionEn = "Calculates the arithmetic mean of numeric cells.",
            descriptionEl = "Υπολογίζει τον αριθμητικό μέσο όρο κελιών.",
            samplePrompt = "Calculate average of values in B2:B50"
        ),
        ExcelFunctionEntry(
            englishName = "AVERAGEIF",
            greekName = "ΜΕΣΟΣ.ΟΡΟΣ.ΑΝ",
            category = "Math & Stats",
            englishSyntax = "=AVERAGEIF(A2:A50, \"Done\", B2:B50)",
            greekSyntax = "=ΜΕΣΟΣ.ΟΡΟΣ.ΑΝ(A2:A50; \"Ολοκληρώθηκε\"; B2:B50)",
            descriptionEn = "Averages cells matching a specified condition.",
            descriptionEl = "Υπολογίζει μέσο όρο για κελιά που πληρούν ένα κριτήριο.",
            samplePrompt = "Average column B only for rows where column A is Done"
        ),
        ExcelFunctionEntry(
            englishName = "COUNT / COUNTA",
            greekName = "ΠΛΗΘΟΣ / ΠΛΗΘΟΣ.Α",
            category = "Math & Stats",
            englishSyntax = "=COUNTA(A2:A100)",
            greekSyntax = "=ΠΛΗΘΟΣ.Α(A2:A100)",
            descriptionEn = "COUNT counts numbers only; COUNTA counts all non-empty cells.",
            descriptionEl = "Η ΠΛΗΘΟΣ μετρά μόνο αριθμούς· η ΠΛΗΘΟΣ.Α μετρά όλα τα μη κενά κελιά.",
            samplePrompt = "Count how many non-empty cells exist in A2:A100"
        ),
        ExcelFunctionEntry(
            englishName = "COUNTIF",
            greekName = "ΜΕΤΡΗΣΗ.ΑΝ",
            category = "Math & Stats",
            englishSyntax = "=COUNTIF(A2:A100, \">50\")",
            greekSyntax = "=ΜΕΤΡΗΣΗ.ΑΝ(A2:A100; \">50\")",
            descriptionEn = "Counts cells that satisfy a condition.",
            descriptionEl = "Μετρά πόσα κελιά πληρούν μια συγκεκριμένη συνθήκη.",
            samplePrompt = "Count how many items in A2:A100 are greater than 50"
        ),
        ExcelFunctionEntry(
            englishName = "COUNTIFS",
            greekName = "ΜΕΤΡΗΣΗ.ΑΝ.ΣΥΝΟΛΑ",
            category = "Math & Stats",
            englishSyntax = "=COUNTIFS(A2:A100, \"Yes\", B2:B100, \">=2026\")",
            greekSyntax = "=ΜΕΤΡΗΣΗ.ΑΝ.ΣΥΝΟΛΑ(A2:A100; \"Ναι\"; B2:B100; \">=2026\")",
            descriptionEn = "Counts rows matching multiple criteria.",
            descriptionEl = "Μετρά γραμμές που ικανοποιούν πολλαπλά κριτήρια.",
            samplePrompt = "Count rows where column A is Yes and column B is 2026 or later"
        ),
        ExcelFunctionEntry(
            englishName = "ROUND",
            greekName = "ΣΤΡΟΓΓΥΛΟΠΟΙΗΣΗ",
            category = "Math & Stats",
            englishSyntax = "=ROUND(A2*1.24, 2)",
            greekSyntax = "=ΣΤΡΟΓΓΥΛΟΠΟΙΗΣΗ(A2*1,24; 2)",
            descriptionEn = "Rounds a number to a given number of decimal places.",
            descriptionEl = "Στρογγυλοποιεί αριθμό σε συγκεκριμένα δεκαδικά ψηφία (προσοχή: κόμμα , στα δεκαδικά).",
            samplePrompt = "Add 24% VAT to A2 and round to 2 decimal places"
        ),
        ExcelFunctionEntry(
            englishName = "MAX / MIN",
            greekName = "ΜΕΓΙΣΤΟ / ΕΛΑΧΙΣΤΟ",
            category = "Math & Stats",
            englishSyntax = "=MAX(C2:C100)",
            greekSyntax = "=ΜΕΓΙΣΤΟ(C2:C100)",
            descriptionEn = "Finds the largest or smallest number in a range.",
            descriptionEl = "Βρίσκει τη μεγαλύτερη ή μικρότερη τιμή σε μια περιοχή.",
            samplePrompt = "Find the highest and lowest price in column C"
        ),
        ExcelFunctionEntry(
            englishName = "VLOOKUP",
            greekName = "ΚΑΘΕΤΗ.ΑΝΑΖΗΤΗΣΗ",
            category = "Lookup",
            englishSyntax = "=VLOOKUP(E2, A2:C100, 3, FALSE)",
            greekSyntax = "=ΚΑΘΕΤΗ.ΑΝΑΖΗΤΗΣΗ(E2; A2:C100; 3; ΨΕΥΔΗΣ)",
            descriptionEn = "Looks up a value in the first column of a table and returns a column to the right.",
            descriptionEl = "Αναζητά τιμή στην πρώτη στήλη πίνακα και επιστρέφει τιμή από άλλη στήλη.",
            samplePrompt = "Look up product ID E2 in A2:C100 and return column 3 exact match"
        ),
        ExcelFunctionEntry(
            englishName = "XLOOKUP",
            greekName = "XLOOKUP",
            category = "Lookup",
            englishSyntax = "=XLOOKUP(E2, A2:A100, C2:C100, \"Not Found\")",
            greekSyntax = "=XLOOKUP(E2; A2:A100; C2:C100; \"Δεν βρέθηκε\")",
            descriptionEn = "Modern flexible lookup (any direction, built-in not-found fallback). Keeps English name in Greek Excel 365.",
            descriptionEl = "Σύγχρονη αναζήτηση προς κάθε κατεύθυνση με προεπιλεγμένη τιμή αν δεν βρεθεί (χρησιμοποιεί ; στο ελληνικό Excel 365).",
            samplePrompt = "Use XLOOKUP to find E2 in column A and return matching value from column C"
        ),
        ExcelFunctionEntry(
            englishName = "INDEX + MATCH",
            greekName = "ΕΥΡΕΤΗΡΙΟ + ΤΑΙΡΙΑΣΜΑ",
            category = "Lookup",
            englishSyntax = "=INDEX(C2:C100, MATCH(E2, A2:A100, 0))",
            greekSyntax = "=ΕΥΡΕΤΗΡΙΟ(C2:C100; ΤΑΙΡΙΑΣΜΑ(E2; A2:A100; 0))",
            descriptionEn = "Classic left/right lookup that works in every Excel version.",
            descriptionEl = "Κλασική αναζήτηση αριστερά/δεξιά συμβατή με όλες τις εκδόσεις Excel.",
            samplePrompt = "Lookup E2 in column A and return corresponding value from column C using INDEX MATCH"
        ),
        ExcelFunctionEntry(
            englishName = "FILTER",
            greekName = "ΦΙΛΤΡΟ",
            category = "Dynamic Array",
            englishSyntax = "=FILTER(A2:C100, B2:B100=\"Active\", \"None\")",
            greekSyntax = "=ΦΙΛΤΡΟ(A2:C100; B2:B100=\"Ενεργό\"; \"Κανένα\")",
            descriptionEn = "Returns all rows matching a condition dynamically (Excel 365).",
            descriptionEl = "Επιστρέφει δυναμικά όλες τις γραμμές που πληρούν το κριτήριο (Excel 365).",
            samplePrompt = "Filter table A2:C100 to show only rows where column B equals Active"
        ),
        ExcelFunctionEntry(
            englishName = "UNIQUE",
            greekName = "ΜΟΝΑΔΙΚΑ",
            category = "Dynamic Array",
            englishSyntax = "=UNIQUE(A2:A100)",
            greekSyntax = "=ΜΟΝΑΔΙΚΑ(A2:A100)",
            descriptionEn = "Extracts distinct values from a range (Excel 365).",
            descriptionEl = "Εξάγει τις μοναδικές τιμές από μια λίστα χωρίς διπλότυπα (Excel 365).",
            samplePrompt = "Get a list of unique customer names from A2:A100 sorted alphabetically"
        ),
        ExcelFunctionEntry(
            englishName = "SORT",
            greekName = "ΤΑΞΙΝΟΜΗΣΗ",
            category = "Dynamic Array",
            englishSyntax = "=SORT(A2:C50, 3, -1)",
            greekSyntax = "=ΤΑΞΙΝΟΜΗΣΗ(A2:C50; 3; -1)",
            descriptionEn = "Sorts a range dynamically by column index (1 asc, -1 desc).",
            descriptionEl = "Ταξινομεί δυναμικά μια περιοχή με βάση στήλη (1 αύξουσα, -1 φθίνουσα).",
            samplePrompt = "Sort A2:C50 by the 3rd column from highest to lowest"
        ),
        ExcelFunctionEntry(
            englishName = "LEFT / RIGHT / MID",
            greekName = "ΑΡΙΣΤΕΡΑ / ΔΕΞΙΑ / ΜΕΣΑ",
            category = "Text",
            englishSyntax = "=LEFT(A2, 4)",
            greekSyntax = "=ΑΡΙΣΤΕΡΑ(A2; 4)",
            descriptionEn = "Extracts characters from the start, end, or middle of text.",
            descriptionEl = "Εξάγει χαρακτήρες από την αρχή, το τέλος ή το μέσο κειμένου.",
            samplePrompt = "Extract the domain name after @ from an email address in A2"
        ),
        ExcelFunctionEntry(
            englishName = "TRIM",
            greekName = "ΠΕΡΙΚΟΠΗ",
            category = "Text",
            englishSyntax = "=TRIM(A2)",
            greekSyntax = "=ΠΕΡΙΚΟΠΗ(A2)",
            descriptionEn = "Removes extra leading, trailing, and duplicate spaces.",
            descriptionEl = "Αφαιρεί περιττά κενά στην αρχή, το τέλος και ανάμεσα στις λέξεις.",
            samplePrompt = "Clean extra spaces and capitalize first letters of name in A2"
        ),
        ExcelFunctionEntry(
            englishName = "TEXT",
            greekName = "ΚΕΙΜΕΝΟ",
            category = "Text",
            englishSyntax = "=TEXT(A2, \"yyyy-mm-dd\")",
            greekSyntax = "=ΚΕΙΜΕΝΟ(A2; \"εεεε-μμ-ηη\")",
            descriptionEn = "Formats a number or date as text using a custom format code.",
            descriptionEl = "Μορφοποιεί αριθμό ή ημερομηνία ως κείμενο (προσοχή στα ελληνικά format codes).",
            samplePrompt = "Format date in A2 as month name and year"
        ),
        ExcelFunctionEntry(
            englishName = "CONCAT / TEXTJOIN",
            greekName = "ΣΥΝΕΝΩΣΗ / TEXTJOIN",
            category = "Text",
            englishSyntax = "=TEXTJOIN(\", \", TRUE, A2:C2)",
            greekSyntax = "=TEXTJOIN(\", \"; ΑΛΗΘΗΣ; A2:C2)",
            descriptionEn = "Joins multiple text cells together with a delimiter, optionally skipping blanks.",
            descriptionEl = "Ενώνει κελιά κειμένου με διαχωριστικό παραλείποντας τα κενά.",
            samplePrompt = "Combine first name in A2 and last name in B2 with a space"
        ),
        ExcelFunctionEntry(
            englishName = "TODAY / NOW",
            greekName = "ΣΗΜΕΡΑ / ΤΩΡΑ",
            category = "Date & Time",
            englishSyntax = "=TODAY()",
            greekSyntax = "=ΣΗΜΕΡΑ()",
            descriptionEn = "Returns the current date (TODAY) or date + time (NOW).",
            descriptionEl = "Επιστρέφει τη σημερινή ημερομηνία (ΣΗΜΕΡΑ) ή ημερομηνία & ώρα (ΤΩΡΑ).",
            samplePrompt = "Calculate how many days remain until the due date in A2"
        ),
        ExcelFunctionEntry(
            englishName = "EOMONTH",
            greekName = "ΤΕΛΟΣ.ΜΗΝΑ",
            category = "Date & Time",
            englishSyntax = "=EOMONTH(A2, 0)",
            greekSyntax = "=ΤΕΛΟΣ.ΜΗΝΑ(A2; 0)",
            descriptionEn = "Returns the last day of the month N months away.",
            descriptionEl = "Επιστρέφει την τελευταία ημέρα του μήνα μετά από Ν μήνες.",
            samplePrompt = "Find the last day of the month for the date in A2"
        ),
        ExcelFunctionEntry(
            englishName = "NETWORKDAYS",
            greekName = "ΕΡΓΑΣΙΜΕΣ.ΗΜΕΡΕΣ",
            category = "Date & Time",
            englishSyntax = "=NETWORKDAYS(A2, B2)",
            greekSyntax = "=ΕΡΓΑΣΙΜΕΣ.ΗΜΕΡΕΣ(A2; B2)",
            descriptionEn = "Counts working days (Mon–Fri) between two dates.",
            descriptionEl = "Υπολογίζει τις εργάσιμες ημέρες (Δευ–Παρ) ανάμεσα σε δύο ημερομηνίες.",
            samplePrompt = "Count business days between start date A2 and end date B2"
        ),
        ExcelFunctionEntry(
            englishName = "PMT",
            greekName = "ΚΑΤΑΒΟΛΗ",
            category = "Financial",
            englishSyntax = "=PMT(5%/12, 60, -10000)",
            greekSyntax = "=ΚΑΤΑΒΟΛΗ(5%/12; 60; -10000)",
            descriptionEn = "Calculates the constant periodic loan payment.",
            descriptionEl = "Υπολογίζει τη μηνιαία δόση δανείου με σταθερό επιτόκιο.",
            samplePrompt = "Calculate monthly loan payment for amount in A2, annual rate B2, years in C2"
        ),
        ExcelFunctionEntry(
            englishName = "NPV",
            greekName = "ΚΑΘΑΡΗ.ΠΑΡΟΥΣΑ.ΑΞΙΑ",
            category = "Financial",
            englishSyntax = "=NPV(A2, B2:B6)+B1",
            greekSyntax = "=ΚΑΘΑΡΗ.ΠΑΡΟΥΣΑ.ΑΞΙΑ(A2; B2:B6)+B1",
            descriptionEn = "Calculates Net Present Value of future cash flows.",
            descriptionEl = "Υπολογίζει την Καθαρή Παρούσα Αξία μελλοντικών ταμειακών ροών.",
            samplePrompt = "Compute NPV with discount rate in A2, initial investment B1, cash flows B2:B6"
        )
    )
}
