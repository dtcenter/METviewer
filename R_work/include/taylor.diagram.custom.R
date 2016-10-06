library(plotrix)
taylor.diagram.custom<-function(data,col,pch,pos.cor,show.gamma ){
    # Add the col and pch vectors as columns to the 'data' dataframe
    data$COL <- col
    data$PCH <- pch

    # Check that all OSTDEV values are the same. If not error out and exit.
    ostdev <- data$OSTDEV
    ostdev.val <- ostdev[1]
    stopifnot(all(ostdev == ostdev.val))



    # Iterate over each row of the dataframe, 'data', col, and pch
    num_rows <- nrow(data)
    for(i in 1:num_rows){
        if (i == 1){
            # First time drawing, set add=FALSE
            met.taylor.diagram(ref=data[i,],add=FALSE,col=data[i,6], pch=data[i,7], pos.cor=pos.cor, show.gamma = show.gamma)
        }else{
            # Subsequent point, adding to the original plot, set add=TRUE
            met.taylor.diagram(ref=data[i,], add=TRUE, col = col[i], pch=pch[i], pos.cor=pos.cor, show.gamma = show.gamma)
        }
    }
}



met.taylor.diagram<-function(ref,add,col,pch,pos.cor,
    show.gamma, ngamma = 3, gamma.col = 8, sd.arcs = 1, ref.sd = TRUE,
    grad.corr.lines = c(0.2, 0.4, 0.6, 0.8, 0.9), pcex = 1, cex.axis = 1,
    normalize = FALSE, mar = c(2, 4, 6, 6), ...) {

    grad.corr.full <- c(0, 0.2, 0.4, 0.6, 0.8, 0.9, 0.95, 0.99,
        1)

    # Check if the user input a list only (containing the std deviations (model/fcst and
    # reference), centered root mean square error, and Pearson R correlation parameter.
    R<-ref["PR_CORR"]
    sd.f<-ref["FSTDEV"]
    sd.r<-ref["OSTDEV"]
    E2<-ref["RMSE"]
    # Get max SD (OSTDEV or FSTDEV)
    maxsd <- 1.5 * max(sd.f,sd.r)

    if (normalize) {
        sd.f <- sd.f/sd.r
        sd.r <- 1
    }

    oldpar <- par("mar", "xpd", "xaxs", "yaxs")
    min_pin<- min( par("pin") ) - 0.4
    if (!add) {
    # display the diagram
       if (pos.cor) {
            par(mar = mar, pin=c( min_pin, min_pin) )
            plot(0, xlim = c(0, maxsd), ylim = c(0, maxsd), xaxs = "i",
                yaxs = "i", axes = FALSE, xlab=" ", ylab=" ",
                type = "n", cex = cex.axis, ...)
            if (grad.corr.lines[1]) {
                for (gcl in grad.corr.lines) lines(c(0, maxsd *
                  gcl), c(0, maxsd * sqrt(1 - gcl^2)), lty = 3)
            }

           # add the axes
           segments(c(0, 0), c(0, 0), c(0, maxsd), c(maxsd,
                0))
            axis.ticks <- pretty(c(0, maxsd))
            axis.ticks <- axis.ticks[axis.ticks <= maxsd]
            axis(1, at = axis.ticks, cex.axis = cex.axis)
            axis(2, at = axis.ticks, cex.axis = cex.axis)
            if (sd.arcs[1]) {
                if (length(sd.arcs) == 1)
                  sd.arcs <- axis.ticks
                for (sdarc in sd.arcs) {
                  xcurve <- cos(seq(0, pi/2, by = 0.03)) * sdarc
                  ycurve <- sin(seq(0, pi/2, by = 0.03)) * sdarc
                  lines(xcurve, ycurve, col = "blue", lty = 3)
                }
            }
            if (show.gamma[1]) {
                # if the user has passed a set of gamma values, use that
                if (length(show.gamma) > 1){
                    gamma <- show.gamma
                }
                else {
                    # otherwise make up a set
                    gamma <- pretty(c(0, maxsd), n = ngamma)[-1]
                }

                if (gamma[length(gamma)] > maxsd){
                  gamma <- gamma[-length(gamma)]
                }

                labelpos <- seq(45, 70, length.out = length(gamma))

               # do the gamma curves
               for (gindex in 1:length(gamma)) {
                  # Vector addition doesn't seem to behave as originally
                  # coded (cos(seq(0,pi,by=0.03)*gamma[gindex]+sd.r). So create
                  # a scalar value to add to the sequence.
                  sd.val <- sd.r[1,1]
                  xcurve <- cos(seq(0, pi, by = 0.03)) * gamma[gindex]  + sd.val
                  # find where to clip the curves
                  # determine which indices in xcurve are less than 0
                  endcurve <- which(xcurve < 0)
                  endcurve <- ifelse(length(endcurve), min(endcurve) -
                    1, 105)
                  ycurve <- sin(seq(0, pi, by = 0.03)) * gamma[gindex]
                  maxcurve <- xcurve * xcurve + ycurve * ycurve
                  startcurve <- which(maxcurve > maxsd * maxsd)
                  startcurve <- ifelse(length(startcurve), max(startcurve) +
                    1, 0)
                  lines(xcurve[startcurve:endcurve], ycurve[startcurve:endcurve],
                    col = gamma.col)
                  if (xcurve[labelpos[gindex]] > 0)
                    boxed.labels(xcurve[labelpos[gindex]], ycurve[labelpos[gindex]],
                      gamma[gindex], border = FALSE)
                }
            }

            # the outer curve for correlation
            xcurve <- cos(seq(0, pi/2, by = 0.01)) * maxsd
            ycurve <- sin(seq(0, pi/2, by = 0.01)) * maxsd
            lines(xcurve, ycurve)
            bigtickangles <- acos(seq(0.1, 0.9, by = 0.1))
            medtickangles <- acos(seq(0.05, 0.95, by = 0.1))
            smltickangles <- acos(seq(0.91, 0.99, by = 0.01))
            segments(cos(bigtickangles) * maxsd, sin(bigtickangles) *
                maxsd, cos(bigtickangles) * 0.97 * maxsd, sin(bigtickangles) *
                0.97 * maxsd)
            par(xpd = TRUE)

            # the inner curve for reference SD
            if (ref.sd) {
                xcurve <- cos(seq(0, pi/2, by = 0.01)) * sd.r
                ycurve <- sin(seq(0, pi/2, by = 0.01)) * sd.r
                lines(xcurve, ycurve)
            }
            points(sd.r, 0, cex = pcex)
            text(cos(c(bigtickangles, acos(c(0.95, 0.99)))) *
                1.05 * maxsd, sin(c(bigtickangles, acos(c(0.95,
                0.99)))) * 1.05 * maxsd, c(seq(0.1, 0.9, by = 0.1),
                0.95, 0.99))
            text(maxsd * 0.8, maxsd * 0.8, "Correlation", srt = 315)
            segments(cos(medtickangles) * maxsd, sin(medtickangles) *
                maxsd, cos(medtickangles) * 0.98 * maxsd, sin(medtickangles) *
                0.98 * maxsd)
            segments(cos(smltickangles) * maxsd, sin(smltickangles) *
                maxsd, cos(smltickangles) * 0.99 * maxsd, sin(smltickangles) *
                0.99 * maxsd)
        }
        else {
            if (add == FALSE) {
            # pourtour du diagramme (display the diagram)
               maxray <- 1.5 * max(sd.f, sd.r)
                plot(c(-maxray,maxray), c(0, maxray), xlab= "", ylab="", type = "n",
                  asp = 1, bty = "n", xaxt = "n", yaxt = "n",
                  cex = cex.axis)
                discrete <- seq(180, 0, by = -1)
                listepoints <- NULL
                for (i in discrete) {
                  listepoints <- cbind(listepoints, maxray *
                    cos(i * pi/180), maxray * sin(i * pi/180))
                }
                listepoints <- matrix(listepoints, 2, length(listepoints)/2)
                listepoints <- t(listepoints)
                lines(listepoints[, 1], listepoints[, 2])

               # axes x,y
               lines(c(-maxray, maxray), c(0, 0))
               lines(c(0, 0), c(0, maxray))

               # lignes radiales jusque R = +/- 0.8
               for (i in grad.corr.lines) {
                  lines(c(0, maxray * i), c(0, maxray * sqrt(1 -
                    i^2)), lty = 3)
                  lines(c(0, -maxray * i), c(0, maxray * sqrt(1 -
                    i^2)), lty = 3)
                }

                 # texte radial
                 for (i in grad.corr.full) {
                    text(1.05 * maxray * i, 1.05 * maxray * sqrt(1 -
                         i^2), i, cex = 0.6)
                    text(-1.05 * maxray * i, 1.05 * maxray * sqrt(1 -
                         i^2), -i, cex = 0.6 )
                }

               # sd concentriques autour de la reference
               seq.sd <- seq.int(0, 2 * maxray, by = (maxray/10))[-1]
                for (i in seq.sd) {
                  xcircle <- sd.r + (cos(discrete * pi/180) *
                    i)
                  ycircle <- sin(discrete * pi/180) * i
                  for (j in 1:length(xcircle)) {
                    if ((xcircle[j]^2 + ycircle[j]^2) < (maxray^2)) {
                      points(xcircle[j], ycircle[j], col = "darkgreen",
                        pch = ".")
                      if (j == 10)
                        text(xcircle[j], ycircle[j], signif(i,
                          2), cex = 0.5, col = "darkgreen")
                    }
                  }
                }

              # sd concentriques autour de l'origine
              seq.sd <- seq.int(0, maxray, length.out = 5)
                for (i in seq.sd) {
                  xcircle <- (cos(discrete * pi/180) * i)
                  ycircle <- sin(discrete * pi/180) * i
                  if (i)
                    lines(xcircle, ycircle, lty = 3, col = "blue")
                  text(min(xcircle), -0.03 * maxray, signif(i,
                    2), cex = 0.5, col = "blue")
                  text(max(xcircle), -0.03 * maxray, signif(i,
                    2), cex = 0.5, col = "blue")
                }
                text(0, -0.08 * maxray, "",
                  cex = 0.7, col = "blue")
                text(0, -0.12 * maxray, "",
                  cex = 0.7, col = "darkgreen")
                #points(sd.r, 0, pch = 22, bg = "darkgreen", cex = 1.1)
                #Consistent with pos.cor=TRUE
                points(sd.r, 0, pch = 1, bg = "darkgreen", cex = 1.)
                text(0, 1.1 * maxray, "Correlation",
                 cex = 0.7)
            }
            S <- (2 * (1 + R))/(sd.f + (1/sd.f))^2
            #   Taylor<-S
       }
    }

    # display the points
    points(sd.f * R, sd.f * sin(acos(R)), pch = pch, col = col,
        cex = pcex)
    invisible(oldpar)
}