//  we can write a single curry function to take an undetermined number of functions TODO
/**
 * transforms the function into 2 separate argument functions
 * @param f
 * @returns {function(*=): function(*=): *}
 */
let curryTwo = function(f)  {
    return function(a) {
        return function(b)  {
            return f(a, b);
        }
    }
}

/**
 * transforms the passed in function to 3 separate argument functions
 * @param f
 * @returns {function(*=): function(*=): function(*=): *}
 */
let curryThree = function(f)  {
    return function(a)  {
        return function(b) {
            return function(c) {
                return f(a, b, c);
            }
        }
    }
}

/**
 * transforms the passed in function to 4 separate argument functions
 * @param f
 * @returns {function(*=): function(*=): function(*=): function(*=): *}
 */
let curryFour = function(f)  {
    return function(a)  {
        return function(b) {
            return function(c) {
                return function(d) {
                    return f(a, b, c, d);
                }
            }
        }
    }
}

/**
 * transforms the passed in function to 5 separate argument functions
 * @param f
 * @returns {function(*=): function(*=): function(*=): function(*=): function(*=):*}
 */
let curryFive = function(f)  {
    return function(a)  {
        return function(b) {
            return function(c) {
                return function(d) {
                    return function(e) {
                        return f(a, b, c, d, e);
                    }
                }
            }
        }
    }
}


let curriedTrustmarkRecipientIdentifier = curryFour(renderTrustmarkRecipientIdentifiers);

let trustmarkRecipientIdentifierDetail = curryFour(renderTrustmarkRecipientIdentifiersForm);