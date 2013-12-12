# boxplot_ci() re-implements the boxplot function but adds an argument named "alpha".
# alpha value is used in calculation of confidence intervals
# The default value for alpha is 0.05, which yields a 95% confidence level.

boxplot_ci <-
function (x, ..., range = 1.5, width = NULL, varwidth = FALSE,
    notch = FALSE, outline = TRUE, names, plot = TRUE, border = par("fg"),
    col = NULL, log = "", pars = list(boxwex = 0.8, staplewex = 0.5,
    outwex = 0.5), horizontal = FALSE, add = FALSE, at = NULL, alpha=0.05)
{
    args <- list(x, ...)
    namedargs <- if (!is.null(attributes(args)$names))
        attributes(args)$names != ""
    else rep(FALSE, length.out = length(args))
    groups <- if (is.list(x))
        x
    else args[!namedargs]
    if (0L == (n <- length(groups)))
        stop("invalid first argument")
    if (length(class(groups)))
        groups <- unclass(groups)
    if (!missing(names))
        attr(groups, "names") <- names
    else {
        if (is.null(attr(groups, "names")))
            attr(groups, "names") <- 1L:n
        names <- attr(groups, "names")
    }
    cls <- sapply(groups, function(x) class(x)[1L])
    cl <- if (all(cls == cls[1L]))
        cls[1L]
    else NULL
    for (i in 1L:n) groups[i] <- list(boxplot.stats_ci(unclass(groups[[i]]),
        range, alpha = alpha))
    stats <- matrix(0, nrow = 5L, ncol = n)
    conf <- matrix(0, nrow = 2L, ncol = n)
    ng <- out <- group <- numeric(0L)
    ct <- 1
    for (i in groups) {
        stats[, ct] <- i$stats
        conf[, ct] <- i$conf
        ng <- c(ng, i$n)
        if ((lo <- length(i$out))) {
            out <- c(out, i$out)
            group <- c(group, rep.int(ct, lo))
        }
        ct <- ct + 1
    }
    if (length(cl) && cl != "numeric")
        oldClass(stats) <- cl
    z <- list(stats = stats, n = ng, conf = conf, out = out,
        group = group, names = names)
    if (plot) {
        if (is.null(pars$boxfill) && is.null(args$boxfill))
            pars$boxfill <- col
        do.call("bxp", c(list(z, notch = notch, width = width,
            varwidth = varwidth, log = log, border = border,
            pars = pars, outline = outline, horizontal = horizontal,
            add = add, at = at), args[namedargs]))
        invisible(z)
    }
    else z
}

boxplot.stats_ci <-
function (x, coef = 1.5, do.conf = TRUE, do.out = TRUE, alpha = 0.05)
{
    if (coef < 0)
        stop("'coef' must not be negative")
    nna <- !is.na(x)
    n <- sum(nna)
    stats <- stats::fivenum(x, na.rm = TRUE)
    iqr <- diff(stats[c(2, 4)])
    if (coef == 0)
        do.out <- FALSE
    else {
        out <- if (!is.na(iqr)) {
            x < (stats[2L] - coef * iqr) | x > (stats[4L] + coef *
                iqr)
        }
        else !is.finite(x)
        if (any(out[nna], na.rm = TRUE))
            stats[c(1, 5)] <- range(x[!out], na.rm = TRUE)
    }

    # For confidence interval definition, see Equation 6 from:
    # http://nldr.library.ucar.edu/repository/assets/technotes/TECH-NOTE-000-000-000-846.pdf
    conf <- if (do.conf) {
        z = qnorm(1 - alpha/2)
        b = sqrt(pi/2) * round((z + z/sqrt(2))/2, 1) / 1.349
        stats[3L] + c(-1.0*b, b) * iqr/sqrt(n)
    }
    list(stats = stats, n = n, conf = conf, out = if (do.out) x[out & nna] else numeric())
}
