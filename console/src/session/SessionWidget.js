import React from "react";
import PropTypes from "prop-types";
import {Dropdown} from "semantic-ui-react";

const sessionWidget = ({displayName, orgName, onLogout}) => {
    const logOut = (ev) => {
        ev.preventDefault();
        onLogout();
    };

    // TODO React 6 migration
    // it should be possible to return array of elements instead of wrapping in a div
    return <div>
        <Dropdown item text={displayName}>
            <Dropdown.Menu>
                <Dropdown.Item icon="log out" onClick={logOut} content="Log out"/>
            </Dropdown.Menu>
        </Dropdown>
    </div>;
};

sessionWidget.propTypes = {
    displayName: PropTypes.string.isRequired,
    orgName: PropTypes.string.isRequired,
    onLogout: PropTypes.func.isRequired
};

export default sessionWidget;
