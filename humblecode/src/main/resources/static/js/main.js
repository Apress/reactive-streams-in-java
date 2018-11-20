
var errorHandler = (err) => { alert("Sorry, there was a problem. " + err); console.log(err) }

var HC = {
    loadCourses: function() {
        jQuery.ajax({method: 'get', url: '/api/courses'}).done(
            function(data) {
                var list = data;
                var ul = jQuery('<ul class="courses btn-group"></ul>');
                list.forEach((crs) => {
                    ul.append('<li class="btn-link" onclick="HC.loadCourse(\''+crs.id+'\'); return false">'
                        + crs.name + ': <i>' + HC.toDollars(crs.price) + '</i></li>')
                });
                jQuery('#content').html(ul);
            }
        ).fail( errorHandler );
    },
    toDollars: function(price) {
        return '$' + (price/ 100);
    },
    course: null,
    currentSegment: null,
    nextSegment: null,
    loadCourse: function(id) {
        jQuery.ajax({method: 'get', url: '/api/course/'+id}).done(
            function(course) {
                console.log("data=" + course);
                HC.course = course;
                var segments = course.segments;
                var ul = jQuery('<ul class="segments btn-group"></ul>');
                segments.forEach((segment) => {
                    ul.append('<li class="btn-link" onclick="HC.loadSegment(' + segment.id + ')">'
                        + segment.name + '</li>')
                });
                jQuery('h1').text(course.name);
                jQuery('#content').html(ul);
            }
        ).fail( errorHandler );
    },
    loadSegment: function(id) {
        if (!HC.course) {
            console.log("ERROR: course not available");
            return;
        } else {
            var ids = HC.course.segments.map(it => it.id);
            var index = ids.indexOf(id);
            HC.currentSegment = HC.course.segments[index];
            if (HC.course.segments.length > index) HC.nextSegment = HC.course.segments[index + 1];
            else HC.nextSegment = null;
            //TODO: improve styling of text
            jQuery('h1').text(HC.currentSegment.name);
            jQuery('#content').text(HC.currentSegment.body);
        }
    },
    postCourse: function() {
        var name = jQuery('#name').val();
        var price = jQuery('#price').val();
        jQuery.ajax({method: 'post', url: '/api/course/', data: {name: name, price: price}}).done(
            function(course) {
                console.log("data=" + course);
                HC.course = course;
                var segments = course.segments;
                var ul = jQuery('<ul class="segments btn-group"></ul>');
                segments.forEach((segment) => {
                    ul.append('<li class="btn-link" onclick="HC.loadSegment(' + segment.id + ')">'
                        + segment.name + '</li>')
                });
                jQuery('h1').text(course.name);
                jQuery('#content').html(ul);
            }
        ).fail( errorHandler );
    }
}

