# MobileBinomialTreePricer: An Android app for running Binomial Trees for pricing American options

This app provides a basic Binomial Tree pricer for pricing American options.
The analytics calculated are Price, Delta, Gamma and Vega for both Call and Put.

The "Run" button launches the calculation, and depending on what tab is selected
it will fill the correspondent output table.

The reference implementation is the Generalized Black-Scholes model, which introduces
the <i>cost of carry</i>. If there is no cost of carry, the carry rate input must
equal the risk-free interest rate.

The technique for pricing American options with a discrete lattice is by means of
the backward induction. The time from valuation date to expiry is discretized
using N steps, and the space discretization uses N steps with values that depend
on the tree type.

In general, the space discretization is a logarithmic discretization
of the underlying space, so an "up" and "down" factors are requested for building the
tree leaves for each and every time step. Similarly, an up and down probability is
associated with the two factors. The choice of these 4 variables characterizes the
tree and defines the accuracy. Usually one chooses this 4 unknowns to match as many
moments of the underlying distribution as possible, and to have risk-neutral
probabilities.

There are two improvements to the usual binomial lattice pricing:
- Smoothing
- Acceleration

When the smoothing is enabled, the second last step of the backward induction is
a Black-Scholes step. This is because in the time discretization the option cannot
be exercised from maturity to the second last time grid value. This reduces the
model artifacts, and smoothens out the convergence when the number of nodes increases.

When the acceleration is enabled, one of the two option type (call or put) is calculated
with the Black-Scholes model. This is because, depending on the values of risk-free
and carry rate, only one of them can be early exercised.

The trees implemented are the:
- Cox-Rubinstein-Ross
- Neutralized Jarrow-Rudd
- Tian
- Leisen-Reimer
- Joshi

All these trees have been extensively studied in
<a href=http://fbe.unimelb.edu.au/__data/assets/pdf_file/0010/2591884/170.pdf>this</a> paper.

As regards the Implied Volatiltity calculation, I have implemented the following root finders:
- Bisection
- Brent
- Toms 348

The latter exhibits the least number of iterations and it's the recommended one. As you find out in the code, the Brent implementation has been adpated from Apache common math library, whilst the Toms 348 has been adapted from user <a href=https://gist.github.com/jtravs/>jtravs</a>.

Finally, this is by no means something finished. The error handling can be improved notifying the user on what went wrong. The user is assumed to be someone with notions of stochastic calculus and/or binomial lattice modelling.

For an online Generalized Black-Scholes calculator that shows formulae and plots
please visit my website <a href=http:option-pricer.ml>option-pricer.ml</a>.
